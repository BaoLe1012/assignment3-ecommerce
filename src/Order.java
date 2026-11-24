public class Order{
    private String orderLd;
    private String userId;
    private String proId;
    private String orderTime;

    //constructor
    public Order(String orderId, String userId, String proId, String orderTime){
        this.orderLd=orderId;
        this.userId=userId;
        this.proId=proId;
        this.orderTime=orderTime;
    }

    //default constructor
    public Order(){
        this.orderLd="";
        this.userId="";
        this.proId="";
        this.orderTime="";
    }

    //getter setter
    public String getOrderId(){
        return orderLd;
    }
    public void setOrderId(String orderId){
        this.orderLd=orderId;
    }
    public String getUserId(){
        return userId;
    }
    public void setUserId(String userId){
        this.userId=userId;
    }
    public String getProId(){
        return proId;
    }
    public void setProId(String proId){
        this.proId=proId;
    }
    public String getOrderTime(){
        return orderTime;
    }
    public void setOrderTime(String orderTime){
        this.orderTime= orderTime;
    }

    @Override 
    public String toString(){
        return String.format("{\"order_id\": \"%s\", \"user_id\": \"%s\", \"pro_id\": \"%s\", \"order_time\": \"%s\" }", this.orderLd, this.userId, this.proId, this.orderTime);
    }
}