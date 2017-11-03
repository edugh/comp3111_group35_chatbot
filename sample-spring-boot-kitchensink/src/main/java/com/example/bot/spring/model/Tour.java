package com.example.bot.spring.model;

public class Tour {
    public String planId;
    public String tourDays;
    public String guideName;
    public String guideAccount;
    public String hotel;
    public int capacity;
    public int minimum;

    public Tour(String planId, String tourDays, String guideName, String guideAccount, String hotel, int capacity, int minimum) {
        this.planId = planId;
        this.tourDays = tourDays;
        this.guideName = guideName;
        this.guideAccount = guideAccount;
        this.hotel = hotel;
        this.capacity = capacity;
        this.minimum = minimum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tour tour = (Tour) o;

        if (capacity != tour.capacity) return false;
        if (minimum != tour.minimum) return false;
        if (planId != null ? !planId.equals(tour.planId) : tour.planId != null) return false;
        if (tourDays != null ? !tourDays.equals(tour.tourDays) : tour.tourDays != null) return false;
        if (guideName != null ? !guideName.equals(tour.guideName) : tour.guideName != null) return false;
        if (guideAccount != null ? !guideAccount.equals(tour.guideAccount) : tour.guideAccount != null) return false;
        return hotel != null ? hotel.equals(tour.hotel) : tour.hotel == null;
    }

    @Override
    public int hashCode() {
        int result = planId != null ? planId.hashCode() : 0;
        result = 31 * result + (tourDays != null ? tourDays.hashCode() : 0);
        result = 31 * result + (guideName != null ? guideName.hashCode() : 0);
        result = 31 * result + (guideAccount != null ? guideAccount.hashCode() : 0);
        result = 31 * result + (hotel != null ? hotel.hashCode() : 0);
        result = 31 * result + capacity;
        result = 31 * result + minimum;
        return result;
    }
}
