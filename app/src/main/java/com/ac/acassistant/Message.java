package com.ac.acassistant;

public class Message {
    private String text; // message body
    private MemberData data; // data of the user that sent this message
    private boolean belongsToCurrentUser; // is this message sent by us?
    private boolean isTemp; // is this message sent by us?
    private String[] temp;


    public Message(String text, MemberData data, boolean belongsToCurrentUser, boolean isTemp, String[] temp) {
        this.text = text;
        this.data = data;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.isTemp = isTemp;
        this.temp = temp;
    }


    public String getText() {
        return text;
    }

    public MemberData getData() {
        return data;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }

    public boolean isTemp(){ return isTemp; }

    public String[] getTemp(){return temp;}
}
