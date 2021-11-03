// -------------------------------------------------------------
//
// This is the EmergencyContact Structure used by the application.
// EmergencyContact data: Name, Surname and Phone.
//
// Author: Aggelos Stamatiou, September 2020
//
// --------------------------------------------------------------

package com.stamatiou.entities;

import java.io.Serializable;

public class EmergencyContact implements Serializable {

    private String name;
    private String surname;
    private String phone;

    public static class Builder {

        private String name;
        private String surname;
        private String phone;

        public Builder() {}

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withSurname(String surname) {
            this.surname = surname;
            return this;
        }

        public Builder withPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public EmergencyContact build() {
            EmergencyContact emergencyContact = new EmergencyContact();
            emergencyContact.name = this.name;
            emergencyContact.surname = this.surname;
            emergencyContact.phone = this.phone;
            return emergencyContact;
        }
    }

    private EmergencyContact() {}

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return "EmergencyContact{" + "name='" + name + '\'' + ", surname='" + surname + '\'' + ", phone='" + phone + '\'' + '}';
    }

}
