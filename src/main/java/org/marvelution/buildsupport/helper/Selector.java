/*
 * Copyright (c) 2023-present Marvelution Holding B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.marvelution.buildsupport.helper;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.function.*;

import org.slf4j.*;

public class Selector
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Selector.class);
    private final Path workdir;
    private final String selector;
    private Set<Path> paths;

    public Selector(
            Path workdir,
            String selector)
    {
        this.workdir = workdir;
        this.selector = selector.startsWith("/") ? selector.substring(1) : selector;
    }

    public Optional<Path> unique()
    {
        scan();
        if (paths.size() == 1)
        {
            return Optional.ofNullable(paths.iterator()
                    .next());
        }
        else
        {
            LOGGER.warn("No unique path using selector {}, found {}", selector, paths.size());
            return Optional.empty();
        }
    }

    public Path requireUnique()
    {
        return unique().orElseThrow(() -> new IllegalArgumentException("Unable to locate a single file using " + selector));
    }

    private void scan()
    {
        if (paths == null)
        {
            Predicate<Path> pathFilter;
            if (selector.contains("*"))
            {
                LOGGER.info("Looking up version artifact using glob: {}", selector);
                PathMatcher pathMatcher = workdir.getFileSystem()
                        .getPathMatcher("glob:" + selector);
                pathFilter = pathMatcher::matches;
            }
            else
            {
                pathFilter = entry -> Objects.equals(workdir.relativize(entry)
                        .toString(), selector);
            }

            paths = new HashSet<>();
            try
            {
                Files.walkFileTree(workdir, new SimpleFileVisitor<>()
                {
                    @Override
                    public FileVisitResult visitFile(
                            Path file,
                            BasicFileAttributes attrs)
                    {
                        if (pathFilter.test(file))
                        {
                            paths.add(file);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            catch (IOException e)
            {
                throw new IllegalStateException("Unable to walk trough directory " + workdir, e);
            }
        }
    }
}
