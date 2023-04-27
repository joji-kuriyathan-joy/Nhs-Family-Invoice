package uk.ac.tees.nhsdemo;


import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

public class ReadWriteUserDetails {
    public String fullName, email, mobile, postcode, nhs_number, resi_address, doB, gender, countrySelected, imageUri;



    public ReadWriteUserDetails(){

    }
    public ReadWriteUserDetails(String fullNameTxt, String emailTxt, String mobileTxt,
                                String postcodeTxt, String nhs_numberTxt, String resi_addressTxt, String txtDoB, String txtGender, String countrySelected, String imageUri){
        this.fullName = fullNameTxt;
        this.email = emailTxt;
        this.mobile = mobileTxt;
        this.postcode = postcodeTxt;
        this.nhs_number = nhs_numberTxt;
        this.resi_address = resi_addressTxt;
        this.doB = txtDoB;
        this.gender = txtGender;
        this.countrySelected = countrySelected;
        this.imageUri = imageUri;


    }

    public ReadWriteUserDetails(String gender, String mobile, String postcode, String nhs_number, String residential_address, String dob, TextView countrySelected) {
    }
}
