package com.itsm.component.moderation;

/**
 * Created by anpiakhota on 26.6.17.
 */
public interface ModerationListener {

    void refresh(int rowIndex);
    void log(String log);

}
