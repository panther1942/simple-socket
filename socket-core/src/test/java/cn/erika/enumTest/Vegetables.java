package cn.erika.enumTest;

public enum Vegetables implements Food {
    西兰花(0x1000),
    大白菜(0x1001),
    黄瓜(0x1002),
    萝卜(0x1003);

    private int value;

    Vegetables(int value) {
        this.value = value;
    }

    @Override
    public void eat() {
        System.out.printf("吃蔬菜: %x\n", this.value);
    }
}
