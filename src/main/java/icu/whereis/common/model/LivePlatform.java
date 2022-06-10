package icu.whereis.common.model;

public class LivePlatform {
    private String id;
    private String name;
    private String url;
    private String domains;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomains() {
        return domains;
    }

    public void setDomains(String domains) {
        this.domains = domains;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
