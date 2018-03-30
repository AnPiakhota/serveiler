package com.itsm.controller;

import com.itsm.controller.bridge.ComAPI;
import com.itsm.controller.data.dto.request.Request;
import com.itsm.controller.data.dto.request.impl.ActionRequest;
import com.itsm.controller.data.dto.request.impl.InfoRequest;
import com.itsm.controller.data.dto.request.impl.ReadRequest;
import com.itsm.controller.data.dto.response.Response;
import com.itsm.controller.data.packet.PacketParserException;
import com.itsm.controller.instance.impl.CP2102;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class provides API to interact with chosen controller.
 * The API is implemented as a set of observable objects
 * using RxJava 2.0
 */
public class ControllerFacade extends AbstractController {

    private ControllerCommunicationException exception;
    private ExecutorService controllerExecutor = Executors.newCachedThreadPool();

    public enum Controller {
        CP2102
    }

    public ControllerFacade(Controller con, ComAPI api) {

        try {
            switch (con) {
                case CP2102:
                    controller = new CP2102<>(api);
                    break;
            }
        } catch (ControllerCommunicationException e) {
            exception = e;
        }

    }

    public Optional<ControllerCommunicationException> isControllerInitialized() {
        return Optional.ofNullable(exception);
    }

    public String[] listAvailablePorts() throws ControllerCommunicationException {
        return controller.listAvailablePorts();
    }

   /*
        Example:
        https://github.com/amitshekhariitbhu/RxJava2-Android-Samples/tree/master/app/src/main/java/com/rxjava2/android/samples
        https://realm.io/news/gotocph-jake-wharton-exploring-rxjava2-android/
    */

    /**
     * The method can be used with PING and VERSION commands
     * otherwise error is returned caused by FacadeApiMishandleException
     * @param request
     * @return
     */
    public Single<Response> inquire(Request request) {

        return Single.fromCallable(() -> {

            if (request instanceof InfoRequest) {
                return controller.request(request);
            } else {
                throw new FacadeApiMishandleException("Only PING and VERSION commands are allowed");
            }


        }).subscribeOn(Schedulers.from(controllerExecutor));

    }

    /**
     * The method can be used with START, STOP, and OUTPUT_VOLTAGE commands
     * otherwise error is returned caused by FacadeApiMishandleException
     * @param request
     * @return
     */
    public Single<Response> action(Request request) {

        return Single.fromCallable(() -> {

            if (request instanceof ActionRequest) {
                return controller.request(request);
            } else {
                throw new FacadeApiMishandleException("Only START, STOP, and OUTPUT_VOLTAGE commands are allowed");
            }


        }).subscribeOn(Schedulers.from(controllerExecutor));

    }

   /**
     * The method can be used with BUFFER commands
     * otherwise error is returned caused by FacadeApiMishandleException
     * @param request
     * @return
     */
    public Flowable<Response> read(Request request, int period) {

        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        stop = false;

        return Flowable.create((FlowableEmitter<Response> emitter) -> {

            emitter.setCancellable(() -> {
                scheduledExecutor.shutdown(); // Called after onComplete
            });

            if (request instanceof ReadRequest) {

                scheduledExecutor.scheduleAtFixedRate(() -> {

                    if (stop) {
                        emitter.onComplete();
                    } else {
                        try {
                            emitter.onNext(controller.request(request));
                        } catch (ControllerCommunicationException | PacketParserException e) {
                            emitter.onError(e);
                        }

                    }

                }, period, period, TimeUnit.MILLISECONDS);

            } else {
                throw new FacadeApiMishandleException("Only BUFFER commands are allowed");
            }

        }, BackpressureStrategy.BUFFER)
        .subscribeOn(Schedulers.from(scheduledExecutor))
        .observeOn(Schedulers.io());

    }

    private volatile boolean stop;

    public void stop() {
        stop = true;
    }




//    public Single<Response> inquire(Request request) {
//
//        return Single.create(emitter -> {
//
//            if (request instanceof InfoRequest) {
//                emitter.onSuccess(controller.request(request));
//            } else {
//                emitter.onError(new Exception("Exception"));
//            }
//        });
//
//    }

//        return Single.fromCallable(() -> {
//
//            switch (request.getToken()) {
//
//                case PING: {
//
//                    Response response = responseFactory.getResponse(Command.PING);
//                    return response;
//
////                    String string = controller.ping();
//
//                }
//
//                case VERSION: {
//
//                    Response response = responseFactory.getResponse(Command.VERSION);
//                    return response;
//
////                    String string = controller.ping();
//
//                }
//
//                default:
//
//                    return null;
//
//            }
//
//        })
//        .subscribeOn(Schedulers.from(controllerExecutor));
//
//    }


//   public Flowable<Response> request(Request request) {
//
//        switch (request.getToken()) {
//
//            case PING:
//
//                return Flowable.fromCallable(() -> {
//
//                    Response response = responseFactory.getResponse(Command.PING);
//                    return response;
//
//                })
//                .subscribeOn(Schedulers.from(controllerExecutor));
//
//            default:
//
//                return null;
//
//        }
//
//    }



//    public Observable<> single =
//
//        Observable.fromCallable(new Callable<String>() {
//
//        @Override
//        public String call() throws Exception {
//            return doSomeTimeTakingIoOperation();
//        }
//    });
//
//
//    Observable.fromCallable(new Callable<String>() {
//        @Override public String call() throws Exception {
//            return client.newCall(request).execute();
//        }
//    });



//        Examples
//        https://github.com/amitshekhariitbhu/RxJava2-Android-Samples
//        https://github.com/ReactiveX/RxJava/wiki/Creating-Observables


//        Observable.create().just(null);
//
//            return new CompletableObserver() {
//                @Override
//                public void onSubscribe(Disposable d) {
//
//                }
//
//                @Override
//                public void onComplete() {
//                }
//
//                @Override
//                public void onError(Throwable e) {
//
//                }
//            };
//        }
//
//
//        // Note that below code is not optimal but it helps in demonstration of concepts
//        // A better version is shown in the next section
//        Observable.create(new Observable.OnSubscribe<String>() {
//            @Override
//            public void call(Subscriber<? super String> subscriber) {
//                try {
//                    String result = doSomeTimeTakingIoOperation();
//                    subscriber.onNext(result);    // Pass on the data to subscriber
//                    subscriber.onCompleted();     // Signal about the completion subscriber
//                } catch (Exception e) {
//                    subscriber.onError(e);        // Signal about the error to subscriber
//                }
//            }
//        });
//
//
//    }
//

}
