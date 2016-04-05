package HelperClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhuol on 2/17/2016.
 */
public class RegexValidator {
    private static Pattern pattern;
    private static Matcher matcher;

    public enum InputType{
        EMAIL,PASSWORD,USERNAME
    }

    private static final String EMAIL_PATTERN ="^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String USERNAME_PATTERN = "^[A-Za-z0-9_-]{3,60}$";
    private static final String PASSWORD_PATTERN="((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,60})";

    /**
     * Validate hex with regular expression
     *
     * @param hex
     *            hex for validation
     * @return true valid hex, false invalid hex
     */
    public static boolean validate(final String hex,InputType type) {
        switch (type){
            case EMAIL: pattern=Pattern.compile(EMAIL_PATTERN);break;
            case USERNAME: pattern=Pattern.compile(USERNAME_PATTERN);break;
            case PASSWORD: pattern=Pattern.compile(PASSWORD_PATTERN);break;
        }
        matcher = pattern.matcher(hex);
        return matcher.matches();
    }
}
