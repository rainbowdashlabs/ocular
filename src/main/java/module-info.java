module ocular {
    requires com.fasterxml.jackson.annotation;
    requires java.compiler;
    requires org.jetbrains.annotations;
    requires org.slf4j;
    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires tools.jackson.dataformat.toml;
    requires tools.jackson.dataformat.yaml;

    exports dev.chojo.ocular;
    exports dev.chojo.ocular.components;
    exports dev.chojo.ocular.dataformats;
    exports dev.chojo.ocular.exceptions;
    exports dev.chojo.ocular.hooks;
    exports dev.chojo.ocular.key;
    exports dev.chojo.ocular.locks;
    exports dev.chojo.ocular.override;
    exports dev.chojo.ocular.processor;

}