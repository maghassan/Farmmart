package challenge.community.farmmart;

public class User {

    public String image, name, contact, address;

    public User(){}

    public User(String image, String name, String contact, String address) {
        this.image = image;
        this.name = name;
        this.contact = contact;
        this.address = address;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
