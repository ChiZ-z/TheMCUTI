package com.iba.service;

import com.iba.exceptions.Exception_400;
import com.iba.exceptions.Exception_404;
import com.iba.model.glossary.Glossary;
import com.iba.model.glossary.GroupItem;
import com.iba.model.glossary.TranslationItem;
import com.iba.model.project.Project;
import com.iba.model.user.User;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Validation class.
 * <p>
 * This class have methods for validate string params.
 */
@Service
public class ValidatorService {

    /**
     * Validate description of project.
     *
     * @param description description of project
     * @return false if validation fails,
     * true if validation completed
     */
    private boolean validateDescription(String description) {
        return Pattern.compile("[^\\^\\{\\}\\[\\]]{1,5000}").matcher(description).matches();
    }

    /**
     * Validate project name.
     *
     * @param projectName project name
     * @return false if validation fails,
     * true if validation completed
     */
    private boolean validateProjectNameOrGlossaryName(String projectName) {
        return Pattern.compile("^[a-zA-Zа-яА-Я0-9\\s]{3,60}$").matcher(projectName).matches();
    }

    /**
     * Validation new project.
     *
     * @param project new project
     * @return true if validation success,
     * false if validation failed.
     */
    public boolean validateProject(Project project) {
        return validateDescription(project.getDescription()) &&
                validateProjectNameOrGlossaryName(project.getProjectName());
    }

    /**
     * Validation username.
     *
     * @param username username
     * @return false if validation fails,
     * * true if validation completed
     */
    public boolean validateUsername(String username) {
        return Pattern.compile("^[a-zA-Z\\d\\._]{4,27}$").matcher(username).matches();
    }

    /**
     * Validation password.
     *
     * @param password password
     * @return false if validation fails,
     * * true if validation completed
     */
    public boolean validatePassword(String password) {
        return Pattern.compile("^[a-zA-Z\\d\\._]{6,27}$").matcher(password).matches();
    }

    /**
     * Validation email.
     *
     * @param email email
     * @return false if validation fails,
     * * true if validation completed
     */
    public boolean validateEmail(String email) {
        return Pattern.compile("^([\\w\\-\\.]+)@((\\[([0-9]{1,3}\\.){3}[0-9]{1,3}\\])|(([\\w\\-]+\\.)+)([a-zA-Z]{2,4}))$").matcher(email).matches();
    }

    /**
     * Validation first name.
     *
     * @param firstName first name
     * @return false if validation fails,
     * * true if validation completed
     */
    public boolean validateFirstName(String firstName) {
        return Pattern.compile("^[a-zA-Zа-яА-Я]{2,27}").matcher(firstName).matches();
    }

    /**
     * Validation last name.
     *
     * @param lastName last name
     * @return false if validation fails,
     * * true if validation completed
     */
    public boolean validateLastName(String lastName) {
        return Pattern.compile("^[a-zA-Zа-яА-Я]{2,27}").matcher(lastName).matches();
    }

    /**
     * Validation registration user fields.
     *
     * @param user registration user
     * @return true if validation success,false if validation failed.
     */
    public boolean validateUser(User user) {
        return validateUsername(user.getUsername()) &&
                validatePassword(user.getPassword()) &&
                validatePassword(user.getRepeatPassword()) &&
                validateEmail(user.getEmail()) &&
                validateFirstName(user.getFirstName()) &&
                validateLastName(user.getLastName());
    }

    /**
     * Validation length of Term value
     *
     * @param term term
     * @return false if validation fails,
     * * true if validation completed
     */
    public boolean validateTermValue(String term) {
//        return Pattern.compile("[a-zA-Zа-яА-Я.\\s\\d]{1,1000}").matcher(term).matches();
        return Pattern.compile(".{1,1000}").matcher(term).matches();
    }

    public boolean validateTermLangValue(String termLang) {
        return Pattern.compile(".{1,5000}").matcher(termLang).matches();
    }

    public boolean validateContactValue(String contact) {
        return Pattern.compile("[+\\d@\\/a-zA-Z_.\\-\\\\]{1,30}").matcher(contact).matches();
    }

    public boolean validateCompanyValue(String company) {
        return Pattern.compile("[\\da-zA-Zа-яА-Я.,\\-\\s]{1,70}").matcher(company).matches();
    }

    public boolean validatePeriodValue(String period) {
        return Pattern.compile("[\\da-zA-Zа-яА-Я.,\\-\\s]{1,70}").matcher(period).matches();
    }

    public boolean validatePositionValue(String position) {
        return Pattern.compile("[\\da-zA-Zа-яА-Я.,\\-\\s]{1,70}").matcher(position).matches();
    }

    public boolean validateActivityValue(String activity) {
        return Pattern.compile(".{1,2000}").matcher(activity).matches();
    }

    public boolean validateLevelValue(String level) {
        return Pattern.compile("[\\da-zA-Zа-яА-Я.,\\-\\s<>:;?!()]{1,200}").matcher(level).matches();
    }

    public boolean validateGlossary(Glossary glossary) {
        return validateDescription(glossary.getDescription()) &&
                validateProjectNameOrGlossaryName(glossary.getGlossaryName());
    }

    public boolean validateGroupItem(GroupItem groupItem) {
        for (TranslationItem translationItem : groupItem.getTranslationItems()) {
            if (!Pattern.compile(".{1,5000}").matcher(translationItem.getTranslationItemValue()).matches()) {
                return false;
            }
        }
        if (groupItem.getComment() != null) {
            return Pattern.compile(".{1,1000}").matcher(groupItem.getComment()).matches();
        }
        return true;
    }

    public boolean validateCategory(String category) {
        return Pattern.compile(".{1,30}").matcher(category).matches();
    }

    public boolean validateTranslationItemValue(String translationItemValue) {
        return Pattern.compile(".{1,5000}").matcher(translationItemValue).matches();
    }

    void validateRandomPeriodDate(String start, String end) {
        if (start == null || end == null) {
            throw new Exception_404("Null dates");
        }
        Pattern pattern = Pattern.compile("[1-9][\\d]{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])");
        if (!pattern.matcher(start).matches() || !pattern.matcher(end).matches()) {
            throw new Exception_400("Invalid data format");
        }
    }

}
