package asliborneo.router.Model;

public class Notification{
    public String title;
    public String body;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Notification() {
    }

    public Notification(String title, String body) {
        this.title = title;
        this.body = body;
    }
}