package com.hulu.ftl;

import com.hulu.ftl.annotations.Literal;
import com.hulu.ftl.annotations.Mapping;
import com.hulu.ftl.annotations.Template;
import java.util.ArrayList;
import java.util.Map;
import java.io.*;

import com.hulu.ftl.exceptions.FTLNotImplemented;
import com.hulu.ftl.formats.JSONFormat;
import com.hulu.ftl.formats.Parser;
import com.hulu.ftl.formats.XMLFormat;
import com.hulu.ftl.formats.XMLFormat2;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


public class FTLDefinition {

    ArrayList<FTLField> fields = new ArrayList<>();

    public FTLDefinition(String configFTL) {

        Constructor constructor = new Constructor();
        constructor.addTypeDescription(new TypeDescription(Literal.class, "!literal"));
        constructor.addTypeDescription(new TypeDescription(Literal.class, "!lit"));
        constructor.addTypeDescription(new TypeDescription(Template.class, "!template"));
        constructor.addTypeDescription(new TypeDescription(Mapping.class, "!map"));

        Yaml yaml = new Yaml(constructor);
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(configFTL);

        Map<String, Object> config = yaml.load(inputStream);

        config.forEach((key, val) -> {
            if (val instanceof ArrayList) {
                ArrayList values = (ArrayList) val;
                for (Object value : values)
                    fields.add(new FTLField(key, value));
            }
            else {
                fields.add(new FTLField(key, val));
            }
        });
    }

    public Map parse(String filename)
            throws IOException, FTLNotImplemented {
        return parse(new File(filename));
    }

    public Map parse(File file)
            throws IOException, FTLNotImplemented {
        String filename = file.getName();

        String format = filename.substring(filename.lastIndexOf("."));
        InputStream stream = new FileInputStream(file);

        return parse(stream, format);
    }

    public Map parse(String body, String format) throws FTLNotImplemented, IOException {
        return parse(new ByteArrayInputStream(body.getBytes()), format);
    }

    public Map parse(InputStream stream, String format) throws FTLNotImplemented, IOException {

        Parser parser;

        switch(format) {
            case ".xml": parser = new XMLFormat2(stream, fields); break;
            case ".json": parser = new JSONFormat(stream); break;
            default: throw new FTLNotImplemented();
        }

        return parser.extract(fields);
    }

}
