package credentials;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import utility.Offensive360Exception;

/**
 * Author: Shyam_Vegi
 * AuthCredentials Class is to store the serverDetails Safely using intellij sdk
 * Only Valid details are saved.
 */

public class AuthCredentials {
    protected final String KEY = "Offensive360";
    public AuthCredentials() {

    }

    private CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(
                CredentialAttributesKt.generateServiceName("Offensive360", key)
        );
    }

    //Checks credentials before storing
    public String checkCredentials() throws Offensive360Exception {
        Credentials credentials = this.getCredentials();

        if(credentials == null){
            throw new Offensive360Exception("Please Configure Offensive360 to Scan");
        }

        String authToken = credentials.getPasswordAsString();
        String serverUrl = credentials.getUserName();

        if (authToken == null || serverUrl == null || authToken.isBlank() || serverUrl.isBlank()) {
            throw new Offensive360Exception("Invalid Credentials Please Check Configuration In Settings");
        }
        return authToken+"@"+serverUrl;
    }

    protected Credentials getCredentials(){
        CredentialAttributes credentialAttributes = createCredentialAttributes(KEY);

        return PasswordSafe.getInstance().get(credentialAttributes);
    }

    //Stores credentials Safely
    public boolean storeDetails(String serverUrl,String authToken){
        try{
            CredentialAttributes credentialAttributes = createCredentialAttributes(KEY);
            Credentials credentials = null;
            if(serverUrl!=null && authToken!=null){
               credentials = new Credentials(serverUrl, authToken);
            }
            PasswordSafe.getInstance().set(credentialAttributes, credentials);
            return true;
        }
        catch(Exception e){
            return false;
        }
    }
}