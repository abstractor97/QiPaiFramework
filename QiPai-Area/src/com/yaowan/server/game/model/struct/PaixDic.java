package com.yaowan.server.game.model.struct;

import java.util.ArrayList;

public class PaixDic {  
    String paixing;  
    ArrayList<Integer> alr;  
    public PaixDic(String paixing, ArrayList<Integer> alr) {  
        super();  
        this.paixing = paixing;  
        this.alr = alr;  
    }  
    public PaixDic() {  
        // TODO Auto-generated constructor stub  
    }  
    public String getPaixing() {  
        return paixing;  
    }  
    public void setPaixing(String paixing) {  
        this.paixing = paixing;  
    }  
    public ArrayList<Integer> getAlr() {  
        return alr;  
    }  
    public void setAlr(ArrayList<Integer> alr) {  
        this.alr = alr;  
    }  
    @Override  
    public String toString() {  
        // TODO Auto-generated method stub  
        return paixing+":"+alr;  
    }  
      
} 