package org.apache.isis.viewer.json.applib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public abstract class Parser<T> {
    
    public T valueOf(List<String> str) {
        if(str == null) {
            return null;
        }
        if(str.size()==0)
            return null;
        return valueOf(str.get(0));
    }
    public T valueOf(String[] str) {
        if(str == null) {
            return null;
        }
        if(str.length==0)
            return null;
        return valueOf(str[0]);
    }
    public abstract T valueOf(String str);
    public abstract String asString(T t);

    public final static Parser<String> forString() {
        return new Parser<String>() {
            @Override
            public String valueOf(String str) {
                return str;
            }
            @Override
            public String asString(String t) {
                return t;
            }
        };
    }

    public static Parser<Date> forDate() {

        return new Parser<Date>() {
            private final SimpleDateFormat RFC1123_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z");

            @Override
            public Date valueOf(String str) {
                if(str == null) {
                    return null;
                }
                try {
                    return RFC1123_DATE_FORMAT.parse(str);
                } catch (ParseException e) {
                    return null;
                }
            }
            @Override
            public String asString(Date t) {
                return RFC1123_DATE_FORMAT.format(t);
            }
        };
    }

    public static Parser<CacheControl> forCacheControl() {
        return new Parser<CacheControl>(){
            @Override
            public CacheControl valueOf(String str) {
                if(str == null) {
                    return null;
                }
                final CacheControl cacheControl = CacheControl.valueOf(str);
                // workaround for bug in CacheControl's equals() method
                cacheControl.getCacheExtension(); 
                cacheControl.getNoCacheFields();
                return cacheControl;
            }
            @Override
            public String asString(CacheControl cacheControl) {
                return cacheControl.toString();
            }};
    }

    public static Parser<MediaType> forMediaType() {
        return new Parser<MediaType>() {
            @Override
            public MediaType valueOf(String str) {
                if(str == null) {
                    return null;
                }
                return MediaType.valueOf(str);
            }

            @Override
            public String asString(MediaType t) {
                return t.toString();
            }
            
        };
    }

    public static Parser<Boolean> forBoolean() {
        return new Parser<Boolean>() {
            @Override
            public Boolean valueOf(String str) {
                if(str == null) {
                    return null;
                }
                return str.equals("yes")?Boolean.TRUE:Boolean.FALSE;
            }

            @Override
            public String asString(Boolean t) {
                return t?"yes":"no";
            }
            
        };
    }

    public static Parser<Integer> forInteger() {
        return new Parser<Integer>() {

            @Override
            public Integer valueOf(String str) {
                if(str == null) {
                    return null;
                }
                return Integer.valueOf(str);
            }

            @Override
            public String asString(Integer t) {
                return t.toString();
            }};
    }

    public static Parser<List<String>> forListOfStrings() {
        return new Parser<List<String>>() {

            @Override
            public List<String> valueOf(List<String> strings) {
                if(strings == null) {
                    return Collections.emptyList();
                }
                if(strings.size() == 1) {
                    // special case processing to handle comma-separated values
                    return valueOf(strings.get(0));
                }
                return strings;
            }
            
            @Override
            public List<String> valueOf(String[] strings) {
                if(strings == null) {
                    return Collections.emptyList();
                }
                if(strings.length == 1) {
                    // special case processing to handle comma-separated values
                    return valueOf(strings[0]);
                }
                return Arrays.asList(strings);
            }
            
            @Override
            public List<String> valueOf(String str) {
                if(str == null) {
                    return Collections.emptyList();
                }
                return Lists.newArrayList(Splitter.on(",").split(str));
            }

            @Override
            public String asString(List<String> strings) {
                return Joiner.on(",").join(strings);
            }
        };
    }

    public static Parser<List<List<String>>> forListOfListOfStrings() {
        return new Parser<List<List<String>>>() {

            @Override
            public List<List<String>> valueOf(List<String> str) {
                if(str == null) {
                    return null;
                }
                if(str.size()==0)
                    return null;
                List<List<String>> listOfLists = Lists.newArrayList();
                for (String s : str) {
                    final Iterable<String> split = Splitter.on('.').split(s);
                    listOfLists.add(Lists.newArrayList(split));
                }
                return listOfLists;
            }
            
            @Override
            public List<List<String>> valueOf(String[] str) {
                if(str == null) {
                    return null;
                }
                if(str.length==0)
                    return null;
                return valueOf(Arrays.asList(str));
            }

            @Override
            public List<List<String>> valueOf(String str) {
                if(str == null || str.isEmpty()) {
                    return Collections.emptyList();
                }
                final Iterable<String> listOfStrings = Splitter.on(',').split(str);
                return Lists.transform(Lists.newArrayList(listOfStrings), new Function<String, List<String>>() {

                    @Override
                    public List<String> apply(String input) {
                        return Lists.newArrayList(Splitter.on('.').split(input));
                    }
                });
            }

            @Override
            public String asString(List<List<String>> listOfLists) {
                List<String> listOfStrings = Lists.transform(listOfLists, new Function<List<String>, String>() {
                    @Override
                    public String apply(List<String> listOfStrings) {
                        return Joiner.on('.').join(listOfStrings);
                    }
                });
                return Joiner.on(',').join(listOfStrings);
            }
        };
    }


    public static Parser<String[]> forArrayOfStrings() {
        return new Parser<String[]>() {

            @Override
            public String[] valueOf(List<String> strings) {
                if(strings == null) {
                    return new String[]{};
                }
                if(strings.size() == 1) {
                    // special case processing to handle comma-separated values
                    return valueOf(strings.get(0));
                }
                return strings.toArray(new String[]{});
            }
            
            @Override
            public String[] valueOf(String[] strings) {
                if(strings == null) {
                    return new String[]{};
                }
                if(strings.length == 1) {
                    // special case processing to handle comma-separated values
                    return valueOf(strings[0]);
                }
                return strings;
            }
            
            @Override
            public String[] valueOf(String str) {
                if(str == null) {
                    return new String[]{};
                }
                Iterable<String> split = Splitter.on(",").split(str);
                return Iterables.toArray(split, String.class);
            }

            @Override
            public String asString(String[] strings) {
                return Joiner.on(",").join(strings);
            }
        };
    }
    
    public static Parser<List<MediaType>> forListOfMediaTypes() {
        return new Parser<List<MediaType>>() {

            @Override
            public List<MediaType> valueOf(String str) {
                if(str == null) {
                    return Collections.emptyList();
                }
                final List<String> strings = Lists.newArrayList(Splitter.on(",").split(str));
                return Lists.transform(strings, (Function<? super String, ? extends MediaType>) new Function<String, MediaType>() {

                    @Override
                    public MediaType apply(String input) {
                        return MediaType.valueOf(input);
                    }
                });
            }

            @Override
            public String asString(List<MediaType> listOfMediaTypes) {
                final List<String> strings = Lists.transform(listOfMediaTypes, new Function<MediaType, String>() {
                    @Override
                    public String apply(MediaType input) {
                        return input.toString();
                    }
                });
                return Joiner.on(",").join(strings);
            }
        };
    }
    
}
