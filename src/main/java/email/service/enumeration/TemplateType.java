package email.service.enumeration;

/**
 * 排程種類
 */
public enum TemplateType {
    ACTIVATE ("Account activation"),
    RECALCULATE ("Game result recalculate announce"),
    ;

    private String title;

    TemplateType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
