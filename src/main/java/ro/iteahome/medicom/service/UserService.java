package ro.iteahome.medicom.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ro.iteahome.medicom.exception.business.NotNhsRegisteredDoctorOrNurseException;
import ro.iteahome.medicom.exception.business.UserNotFoundException;
import ro.iteahome.medicom.model.entity.User;
import ro.iteahome.medicom.model.form.UserCreationForm;
import ro.iteahome.medicom.repository.UserRepository;
import ro.iteahome.medicom.security.UserContext;

import java.util.Optional;

@Service
public class UserService {

// DEPENDENCIES: -------------------------------------------------------------------------------------------------------

    private final String DOCTORS_URL = "https://nhsbackendstage.myserverapps.com/doctors";
    private final String NURSES_URL = "https://nhsbackendstage.myserverapps.com/doctors";

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

// C.R.U.D. METHODS: ---------------------------------------------------------------------------------------------------

    // TODO: Add CRUD methods for Users.

    public void add(UserCreationForm userCreationForm) {
        if (isNhsRegistered(userCreationForm)) {
            User user = modelMapper.map(userCreationForm, User.class);
            userRepository.save(user);
        } else {
            throw new NotNhsRegisteredDoctorOrNurseException();
        }
    }

// OTHER METHODS: ------------------------------------------------------------------------------------------------------

    public void logIn(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmailAndPassword(email, password);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            UserContext.setLoggedInUser(user);
        } else {
            throw new UserNotFoundException();
        }
    }
    // TODO: Actually develop a secure log in method.

    public void signOut() {
        UserContext.setLoggedInUser(null);
    }
    // TODO: Actually develop a secure sign out method.

    private Boolean isNhsRegistered(UserCreationForm userCreationForm) {
        Boolean isNhsRegistered;
        switch (userCreationForm.getRole()) {
            case DOCTOR:
                isNhsRegistered = restTemplate.getForObject(
                        DOCTORS_URL
                                .concat("/existence/by-cnp-and-license-number/?cnp=")
                                .concat(userCreationForm.getCnp())
                                .concat("&licenseNo=")
                                .concat(userCreationForm.getLicenseNo()),
                        Boolean.class);
                break;
            case NURSE:
                isNhsRegistered = restTemplate.getForObject(
                        NURSES_URL
                                .concat("/existence/by-cnp-and-license-number/?cnp=")
                                .concat(userCreationForm.getCnp())
                                .concat("&licenseNo=")
                                .concat(userCreationForm.getLicenseNo()),
                        Boolean.class);
                break;
            default:
                isNhsRegistered = null;
        }
        return isNhsRegistered;
    }
}
