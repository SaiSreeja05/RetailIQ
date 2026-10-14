module RetailIQProject {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires java.desktop;
    requires jdk.httpserver;

    exports retailiq;
    exports retailiq.csv;
    exports retailiq.service;
    exports retailiq.ui;
    exports retailiq.web;
    exports DynamicProgramming;
    exports StringAlgorithms;
    exports SuffixStructures;

    opens retailiq.ui to javafx.graphics, javafx.controls, javafx.base;
}