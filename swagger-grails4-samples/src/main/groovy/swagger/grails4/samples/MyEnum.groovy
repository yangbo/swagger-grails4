package swagger.grails4.samples

enum MyEnum {
    Value1(1),
    Value2(2),
    int id

    MyEnum(int id) {
        this.id = id
    }
}
