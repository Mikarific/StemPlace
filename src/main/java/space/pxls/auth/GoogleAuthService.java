package space.pxls.auth;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import space.pxls.App;

public class GoogleAuthService extends AuthService {
    public GoogleAuthService(String id) {
        super(id, App.getConfig().getBoolean("oauth.google.enabled"), App.getConfig().getBoolean("oauth.google.registrationEnabled"));
    }

    @Override
    public String getRedirectUrl(String state) {
        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "scope=profile%20email&" +
                "access_type=online&" +
                "include_granted_scopes=true&" +
                "state=" + state + "&" +
                "redirect_uri=" + getCallbackUrl() + "&" +
                "response_type=code&" +
                "client_id=" + App.getConfig().getString("oauth.google.key");
    }

    @Override
    public String getToken(String code) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.post("https://www.googleapis.com/oauth2/v4/token")
                .header("User-Agent", "pxls.space")
                .field("grant_type", "authorization_code")
                .field("code", code)
                .field("redirect_uri", getCallbackUrl())
                .field("client_id", App.getConfig().getString("oauth.google.key"))
                .field("client_secret", App.getConfig().getString("oauth.google.secret"))
                .asJson();

        JSONObject json = response.getBody().getObject();

        if (json.has("error")) {
            return null;
        } else {
            return json.getString("access_token");
        }
    }

    @Override
    public String getIdentifier(String token) throws UnirestException {
        HttpResponse<JsonNode> me = Unirest.get("https://www.googleapis.com/oauth2/v1/userinfo")
                .header("Authorization", "Bearer " + token)
                .header("User-Agent", "pxls.space")
                .asJson();
        JSONObject json = me.getBody().getObject();
	if (json.has("error")) {
            return null;
        } else {
            if(json.has("hd")) {
                if (json.getString("hd").equals("s.stemk12.org") || json.getString("hd").equals("stemk12.org")) {
                    return json.getString("id");
                } else {
                    return "auth_invalid";
                }
            } else {
                return "auth_invalid";
            }
        }
    }

    public String getName() {
        return "Google";
    }

    @Override
    public void reloadEnabledState() {
        this.enabled = App.getConfig().getBoolean("oauth.google.enabled");
        this.registrationEnabled = App.getConfig().getBoolean("oauth.google.registrationEnabled");
    }
}
