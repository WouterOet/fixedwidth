# fixedwidth

Fixedwidth is a library which helps you parse files with a fixed width format and bind them to Java objects.

Example:

````java
@fixedwidth.annotations.Record
class MyRecord {
    
    @fixedwidth.annotations.Position(start = 0, end = 11)
    private String someString;
    
    @fixedwidth.annotations.Position(start = 11, end = 15)
    private int someInt;
}

FixedWidth<MyRecord> fixedWidth = FixedWidth.forClass(MyRecord.class);
MyRecord record = fixedWidth.parse("Hallo world0042");
````

The supported types are:

* All Java primitives and boxed versions
* LocalDate & LocalDateTime with custom date formats
* Custom types using converters
* Enum types