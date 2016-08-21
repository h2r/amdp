package amdp.taxiamdpdomains.testingtools;

public class MutableGlobalInteger {

    Integer x = -1;

    public MutableGlobalInteger(Integer value){
        this.x=value;
    }

    public MutableGlobalInteger(){};

    public Integer getValue(){
        return this.x;
    }

    public Integer setValue(Integer value){
        return this.x = value;
    }

    public void subtractOne(){
        this.x -= 1;
    }

    public void subtractTwo(){
        this.x -= 2;
    }

    public void addOne(Integer value){
        this.x += 1;
    }

}
