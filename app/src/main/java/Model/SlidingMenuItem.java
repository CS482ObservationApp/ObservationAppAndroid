package Model;

/**
 * Created by zhuol on 3/1/2016.
 */
public class SlidingMenuItem {
    public enum ItemType{
        USER_ACCOUNT_ITEM,LOGIN_ITEM,SEARCH_ITEM,UPLOAD_ITEM,USER_GUIDE_ITEM,MY_POST_ITEM
    }
    public SlidingMenuItem(ItemType itemType){
        this.itemType=itemType;
    }
    public ItemType itemType;
    public String text;
}
