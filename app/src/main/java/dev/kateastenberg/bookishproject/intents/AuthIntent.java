package dev.kateastenberg.bookishproject.intents;

import dev.kateastenberg.bookishproject.models.User;

public abstract class AuthIntent {

    public static class Login extends AuthIntent {
        public final String email;
        public final String password;
        public Login (String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class Logout extends AuthIntent {
        public Logout() {}
    }

    public static class DeleteUser extends AuthIntent {
        public final User user;
        public DeleteUser(User user) {
            this.user = user;
        }
    }

    public static class ResetPassword extends AuthIntent {
        public final String email;
        public ResetPassword(String email) {
            this.email = email;
        }
    }

    public static class Register extends AuthIntent {
        public final String email;
        public final String username;
        public final String password;
        public Register (String email, String username, String password) {
            this.email = email;
            this.username = username;
            this.password = password;
        }
    }

    public static class UpdateUser extends AuthIntent {
        public final User user;
        public final String name;
        public final String email;
        public UpdateUser (User user, String name, String email) {
            this.user = user;
            this.name = name;
            this.email = email;
        }
    }

    /*
    Used by AccountFragment only
     */
    public static class SetEditable extends AuthIntent {
        public SetEditable () {}
    }

    /*
    Used by AccountFragment only
     */
    public static class SetViewOnly extends AuthIntent {
        public SetViewOnly () {}
    }

    public static class ChangePassword extends AuthIntent {
        public final String password;
        public ChangePassword (String password) {
            this.password = password;
        }
    }

    public static class SaveCredentials extends AuthIntent {

    }

    public static class ForgetCredentials extends AuthIntent{

    }

    /*
    Used only by PasswordResetFragment
     */
    public static class OpenEmailApp extends AuthIntent {

    }

    /*
    Used only by PasswordResetFragment
     */
    public static class ShowEmailApps extends AuthIntent {

    }

    /*
    Used only by AccountFragment
     */
    public static class LoadUserData extends AuthIntent {
        public LoadUserData() {
        }
    }

}
