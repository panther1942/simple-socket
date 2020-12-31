package cn.erika.enumTest;

public enum Fruit implements Food {
    苹果(0x2000),
    香蕉(0x2001),
    桃子(0x2002),
    西瓜(0x2003),;

    private int value;

    Fruit(int value) {
        this.value = value;
    }

    @Override
    public void eat() {
        System.out.printf("吃水果: %x\n", this.value);
    }
}
