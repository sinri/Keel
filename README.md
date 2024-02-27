# Keel

![Maven Central](https://img.shields.io/maven-central/v/io.github.sinri/Keel)
![GitHub](https://img.shields.io/github/license/sinri/Keel)

A Java framework with VERT.X eco, for projects for web, job and more.

```xml

<dependency>
    <groupId>io.github.sinri</groupId>
    <artifactId>Keel</artifactId>
  <version>3.1.9</version>
</dependency>
```

## Third Party Integration

* [Vert.x](https://vertx.io) 4.5.4
* [org.commonmark:commonmark](https://github.com/commonmark/commonmark-java) ![GitHub](https://img.shields.io/github/license/commonmark/commonmark-java)
  0.21.0
* [org.reflections:reflections](https://github.com/ronmamo/reflections) ![GitHub](https://img.shields.io/github/license/ronmamo/reflections)
  0.10.2
* [com.warrenstrange:googleauth](https://github.com/wstrange/GoogleAuth) ![GitHub](https://img.shields.io/github/license/wstrange/GoogleAuth)
* [com.github.oshi:oshi-core](https://github.com/oshi/oshi) ![GitHub](https://img.shields.io/github/license/oshi/oshi)
  6.4.0
* [org.apache.poi:poi](https://github.com/apache/poi) ![GitHub](https://img.shields.io/github/license/apache/poi)
  5.2.5

## Dry Dock of Keel

Consider to use [Dry Dock of Keel](https://github.com/sinri/DryDockOfKeel) to build an application quickly!

## Branches

* latest: the latest pushed version of Keel, may not be released yet.
* p3: the latest released version of Keel 3.x.

## Migration

### From 3.0.x to 3.1.x

* The `KeelHelpers` becomes a static member of class `KeelHelpersInterface`.
* The `Keel` becomes a static member of class `KeelInstance` as its instance. Originally it was a class.
  * The `Keel` now also inherits the class `KeelHelpersInterface`. So you may not need `KeelHelpers` while `Keel` used.
* MySQL
  * Data source should be defined as `NamedMySQLDataSource`.
  * The raw `SqlConnection` instance should be used in a defined `NamedMySQLConnection`.
  * `DynamicNamedMySQLConnection` is also available for convenience.
