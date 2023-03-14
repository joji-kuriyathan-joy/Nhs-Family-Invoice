package uk.ac.tees.nhsdemo;


import android.widget.ImageView;

public class ReadWriteUserDetails {
    public String firstname, lastname, email, mobile, postcode, nhs_number, resi_address, doB, gender, countrySelected;
    public ImageView userProfileImage;


    public ReadWriteUserDetails(){

    }
    public ReadWriteUserDetails(String firstnameTxt, String lastnameTxt, String emailTxt, String mobileTxt,
                                String postcodeTxt, String nhs_numberTxt, String resi_addressTxt, String txtDoB, String txtGender, String countrySelected, ImageView userProfileImage){
        this.firstname = firstnameTxt;
        this.lastname = lastnameTxt;
        this.email = emailTxt;
        this.mobile = mobileTxt;
        this.postcode = postcodeTxt;
        this.nhs_number = nhs_numberTxt;
        this.resi_address = resi_addressTxt;
        this.doB = txtDoB;
        this.gender = txtGender;
        this.countrySelected = countrySelected;
        this.userProfileImage = userProfileImage;


    }
}
