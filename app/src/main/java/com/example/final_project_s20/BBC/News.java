package com.example.final_project_s20.BBC;

public class News {

    private String title;
    private String description;
    private String link;
    private String date;

    /**
     * @param title - title of the news
     * @param description - description of the news
     * @param link - link of the news
     * @param date - Date of the news
     */
    public News(String title, String description, String link, String date){
        this.setTitle(title);
        this.setDescription(description);
        this.setLink(link);
        this.setDate(date);
    }

    /**
     * @param s - the title string of the news
     */
    public void setTitle(String s){this.title = s;}

    /**
     * @return the title of the news
     */
    public String getTitle(){return this.title;}

    /**
     * @param s - the description string of the news
     */
    public void setDescription(String s){this.description = s;}

    /**
     * @return the description of the news
     */
    public String getDescription(){return this.description;}

    /**
     * @param s - the link string of the news
     */
    public void setLink(String s){this.link = s;}

    /**
     * @return the link of the news
     */
    public String getLink(){return this.link;}

    /**
     * @param s - the Date string of the news
     */
    public void setDate(String s){this.date = s;}

    /**
     * @return the Date of the news
     */
    public String getDate(){return this.date;}


}
