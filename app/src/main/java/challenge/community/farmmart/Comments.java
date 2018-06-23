package challenge.community.farmmart;

import java.util.Date;

public class Comments {

    private String message, user_id;
    private Date time_stamp;

    public Comments(){}

    public Comments(String message, String user_id, Date time_stamp) {
        this.message = message;
        this.user_id = user_id;
        this.time_stamp = time_stamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(Date time_stamp) {
        this.time_stamp = time_stamp;
    }
}
