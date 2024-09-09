package com.example.audipix;

public class Items {
    int image;
    String text;
    int edit;
    int action;

    public Items(int image, String text,int edit,int action) {
        this.image = image;
        this.text = text;
        this.edit=edit;
        this.action=action;

    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getEdit() {
        return edit;
    }

    public void setEdit(int edit) {
        this.edit = edit;
    }

    public int getAction(){ return action; }

    public void setAction(int action) { this.action = action; }
}
