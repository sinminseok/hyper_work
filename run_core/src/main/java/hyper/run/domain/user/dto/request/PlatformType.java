package hyper.run.domain.user.dto.request;

public enum PlatformType {
    ANDROID,
    IOS;
    public boolean isAndroid(){
        return this == ANDROID;
    }
}