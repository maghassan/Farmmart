package challenge.community.farmmart;

import java.util.Date;

public class DataModel extends BlogPostId{

    public String user_id, image_url, post_content, post_title, image_thumb;
    public Date time_stamp;

    public DataModel(){}

    public DataModel(String user_id, String image_Url, String post_content, String post_title, String image_thumb, Date time_stamp) {
        this.user_id = user_id;
        this.image_url = image_Url;
        this.post_content = post_content;
        this.post_title = post_title;
        this.image_thumb = image_thumb;
        this.time_stamp = time_stamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getPost_content() {
        return post_content;
    }

    public void setPost_content(String post_content) {
        this.post_content = post_content;
    }

    public String getPost_title() {
        return post_title;
    }

    public void setPost_title(String post_title) {
        this.post_title = post_title;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    public Date getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(Date time_stamp) {
        this.time_stamp = time_stamp;
    }
}
