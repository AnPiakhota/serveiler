package com.itsm.config.request;

import com.itsm.controller.ControllerCommunicationException;
import com.itsm.controller.ControllerFacade;
import com.itsm.controller.bridge.ComAPI;
import com.itsm.controller.bridge.impl.SerialPundit;
import com.itsm.controller.data.dto.AbstractInteractionFactory;
import com.itsm.controller.data.dto.response.Response;
import com.itsm.controller.data.dto.InteractionFactoryProducer;
import io.reactivex.subscribers.ResourceSubscriber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by anpiakhota on 12.12.16.
 */
@Deprecated
public class Handler {

    private static Handler instance;

    static {
        try {
            instance = new Handler();
        } catch (Exception e) {
            throw new RuntimeException("Exception while service initialization", e);
        }
    }

    private final ControllerFacade controllerFacade;
    private final AbstractInteractionFactory requestFactory;

    public static synchronized Handler getInstance(){
        return instance;
    }

    private Handler() throws ControllerCommunicationException {
        ComAPI api = new SerialPundit("/dev/ttyUSB0");
        controllerFacade = new ControllerFacade(ControllerFacade.Controller.CP2102, api);
        requestFactory = InteractionFactoryProducer.getFactory("REQUEST");
    }

            /* PING
            Request pingRequest = requestFactory.getRequest(Command.PING);
            controllerFacade.inquire(pingRequest).subscribe(
                onSuccess -> System.out.println(onSuccess.toString()),
                onError -> System.out.println(onError.getStackTrace()));
            */


    public static void main(String[] args) {

        try {
            String line = "";
            Process p = Runtime.getRuntime().exec("whoami");
            InputStream iStream = p.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(iStream);
            BufferedReader bufReader = new BufferedReader(inputStreamReader);
            while ((line = bufReader.readLine()) != null) {
                System.out.println("Input "+line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ComAPI api = new SerialPundit("/dev/ttyUSB0");
            ControllerFacade controllerFacade = new ControllerFacade(ControllerFacade.Controller.CP2102, api);
            AbstractInteractionFactory requestFactory = InteractionFactoryProducer.getFactory("REQUEST");

//            Request versionRequest = requestFactory.getRequest(Command.VERSION);
//            controllerFacade.inquire(pingRequest).subscribe(
//                    onSuccess -> {
//                        System.out.println(onSuccess.getToken());
//                    },
//                    onError -> {
//                        System.out.println("Error");
//                    });



//            controllerFacade.single(pingRequest).subscribe(new SingleObserver<Response>() {
//
//                @Override
//                public void onSubscribe(Disposable d) {
//
//
//                }
//
//                @Override
//                public void onSuccess(Response response) {
//
//
//
//                }
//
//                @Override
//                public void onError(Throwable e) {
//
//                }
//            });


//           controllerFacade.single(pingRequest).subscribe(
//               onSuccess -> {
//                   System.out.println(onSuccess.getToken());
//               },
//               onError -> {
//                   System.out.println("Error");
//               });
//
//           controllerFacade.single(versionRequest).subscribe(
//               onSuccess -> {
//                   System.out.println(onSuccess.getToken());
//               },
//               onError -> {
//                   System.out.println("Error");
//               });

        } catch (ControllerCommunicationException e) {
            e.printStackTrace();
        }

        while (true);

    }

}



