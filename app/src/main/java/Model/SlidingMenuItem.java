package Model;

/**
 * Created by zhuol on 3/1/2016.
 */
public class SlidingMenuItem {
    public enum ItemType{
        USER_ACCOUNT_ITEM,NORMAL_ITEM,LOGIN_ITEM
    }
    public SlidingMenuItem(ItemType itemType){
        this.itemType=itemType;
    }
    public ItemType itemType=ItemType.NORMAL_ITEM;
    public String username="";
    public String location="";
    public String imgFileLocation="";
    public String text="";
}
