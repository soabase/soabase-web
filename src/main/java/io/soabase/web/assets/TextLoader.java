package io.soabase.web.assets;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TextLoader
{
    private final Map<String, Map<String, String>> text;
    private final String textDir;

    public TextLoader(File assets, String textDir)
    {
        text = assets.isDirectory() ? loadTextFromDir(assets) : loadTextFromZip(assets);
        this.textDir = textDir;
    }

    public Map<String, String> getFor(String languageCode)
    {
        Map<String, String> values = text.get(languageCode);
        return (values != null) ? values : ImmutableMap.of();
    }

    private Map<String, Map<String, String>> loadTextFromZip(File assetsFile)
    {
        Map<String, Map<String, String>> text = Maps.newHashMap();
        try
        {
            ZipFile zipFile = new ZipFile(assetsFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while ( entries.hasMoreElements() )
            {
                ZipEntry entry = entries.nextElement();
                File entryFile = new File(entry.getName());
                if ( !entry.isDirectory() && textDir.equals(entryFile.getParent()) )
                {
                    try ( InputStream stream = zipFile.getInputStream(entry) )
                    {
                        text.put(getKey(entryFile.getName()), read(entryFile.getName(), CharStreams.readLines(new InputStreamReader(stream)).stream()));
                    }
                }
            }
        }
        catch ( IOException e )
        {
            throw new RuntimeException(e);
        }
        return text;
    }

    private Map<String, Map<String, String>> loadTextFromDir(File assetsDir)
    {
        File dir = new File(assetsDir, textDir);
        return StreamSupport.stream(Files.fileTreeTraverser().children(dir).spliterator(), false)
            .map(file -> new AbstractMap.SimpleEntry<>(getKey(file.getName()), read(file)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String getKey(String name)
    {
        return Splitter.on(".").splitToList(name).get(0);
    }

    private Map<String, String> read(File file)
    {
        try
        {
            return read(file.getName(), Files.readLines(file, Charsets.UTF_8).stream());
        }
        catch ( IOException e )
        {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> read(String fileName, Stream<String> stream)
    {
        return stream
            .map(line -> splitLine(fileName, line))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<String, String> splitLine(String fileName, String line)
    {
        int index = line.indexOf(' ');
        if ( index < 0 )
        {
            index = line.indexOf('\t');
            if ( index < 0 )
            {
                index = line.indexOf('=');
                if ( index < 0 )
                {
                    throw new RuntimeException(String.format("Badly formed line in %s: %s", fileName, line));
                }
            }
        }
        String field = line.substring(0, index).trim();
        String value = line.substring(index).trim();
        return new AbstractMap.SimpleEntry<>(field, value);
    }
}
