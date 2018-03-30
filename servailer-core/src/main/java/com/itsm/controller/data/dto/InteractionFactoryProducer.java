package com.itsm.controller.data.dto;

/**
 * Created by anpiakhota on 22.10.16.
 */

public class InteractionFactoryProducer {

    public static AbstractInteractionFactory getFactory(String choice){

        if(choice.equalsIgnoreCase("REQUEST")){
            return new RequestFactory();

        }else if(choice.equalsIgnoreCase("RESPONSE")){
            return new ResponseFactory();
        }

        return null;
    }


}
