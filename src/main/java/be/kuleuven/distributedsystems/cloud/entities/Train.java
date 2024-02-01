package be.kuleuven.distributedsystems.cloud.entities;

public class Train {
    private String trainCompany;
    private String trainId;
    private String name;
    private String location;
    private String image;

    public Train() {
    }

    public Train(String trainCompany, String trainId, String name, String location, String image) {
        this.trainCompany = trainCompany;
        this.trainId = trainId;
        this.name = name;
        this.location = location;
        this.image = image;
    }

    public String getTrainCompany() {
        return trainCompany;
    }

    public String getTrainId() {
        return trainId;
    }

    public String getName() {
        return this.name;
    }

    public String getLocation() {
        return this.location;
    }

    public String getImage() {
        return this.image;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Train)) {
            return false;
        }
        var other = (Train) o;
        return this.trainCompany.equals(other.trainCompany)
                && this.trainId.equals(other.trainId);
    }

    @Override
    public int hashCode() {
        return this.trainCompany.hashCode() * this.trainId.hashCode();
    }
}
