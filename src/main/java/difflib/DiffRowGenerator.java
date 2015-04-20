/*
   Copyright 2010 Dmitry Naumenko (dm.naumenko@gmail.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package difflib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import difflib.DiffRow.Tag;
import difflib.myers.Equalizer;
import difflib.myers.MyersDiff;

/**
 * This class for generating DiffRows for side-by-sidy view.
 * You can customize the way of generating. For example, show inline diffs on not, ignoring
 * white spaces or/and blank lines and so on. All parameters for generating are optional. If you do
 * not specify them, the class will use the default values.
 *
 * These values are:
 * showInlineDiffs = false;
 * ignoreWhiteSpaces = true;
 * ignoreBlankLines = true;
 * ...
 *
 * For instantiating the DiffRowGenerator you should use the its builder. Like in example
 * 
 * <code>
 *    DiffRowGenerator generator = new DiffRowGenerator.Builder().showInlineDiffs(true).
 *      ignoreWhiteSpaces(true).columnWidth(100).build();
 * </code>
 *
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public class DiffRowGenerator {
    private static final String NEW_LINE = "\n";
    private static final Joiner LF_JOINER = Joiner.on(NEW_LINE);
    private static final Joiner JOINER = Joiner.on("");

    private static final String DEFAULT_TAG_DELETE = "del";
    private static final String DEFAULT_TAG_INSERT = "ins";
    private static final String DEFAULT_TAG_CHANGE = "span";
    private static final String DEFAULT_CSSCLASS_DELETE = null;
    private static final String DEFAULT_CSSCLASS_INSERT = null;
    private static final String DEFAULT_CSSCLASS_CHANGE = "change";
    private static final DiffAlgorithm<String> DEFAULT_DIFFALGORITHM = new MyersDiff<String>(new Equalizer<String>() {
        public boolean equals(String original, String revised) {
            return Objects.equals(original, revised);
        }
    });

    private final boolean showInlineDiffs;
    private final boolean ignoreWhiteSpaces;
    private final String inlineOriginDeleteTag;
    private final String inlineRevisedInsertTag;
    private final String inlineOriginChangeTag;
    private final String inlineRevisedChangeTag;
    private final String inlineOriginDeleteCssClass;
    private final String inlineRevisedInsertCssClass;
    private final String inlineOriginChangeCssClass;
    private final String inlineRevisedChangeCssClass;
    private final int columnWidth;
    @Nullable
    private final String defaultString;
    private final DiffAlgorithm<String> diffAlgorithm;

    /**
     * This class used for building the DiffRowGenerator.
     * @author dmitry
     *
     */
    public static class Builder {
        private boolean showInlineDiffs = false;
        private boolean ignoreWhiteSpaces = false;
        private String inlineOriginDeleteTag = DEFAULT_TAG_DELETE;
        private String inlineOriginChangeTag = DEFAULT_TAG_CHANGE;
        private String inlineRevisedInsertTag = DEFAULT_TAG_INSERT;
        private String inlineRevisedChangeTag = DEFAULT_TAG_CHANGE;
        private String inlineOriginDeleteCssClass = DEFAULT_CSSCLASS_DELETE;
        private String inlineRevisedInsertCssClass = DEFAULT_CSSCLASS_INSERT;
        private String inlineOriginChangeCssClass = DEFAULT_CSSCLASS_CHANGE;
        private String inlineRevisedChangeCssClass = DEFAULT_CSSCLASS_CHANGE;
        private int columnWidth = -1;
        @Nullable
        private String defaultString = "";
        private DiffAlgorithm<String> diffAlgorithm = DEFAULT_DIFFALGORITHM;

        /**
         * Show inline diffs in generating diff rows or not.
         * @param val the value to set. Default: false.
         * @return builder with configured showInlineDiff parameter
         */
        public Builder showInlineDiffs(boolean val) {
            showInlineDiffs = val;
            return this;
        }

        /**
         * Ignore white spaces in generating diff rows or not.
         * @param val the value to set. Default: true.
         * @return builder with configured ignoreWhiteSpaces parameter
         */
        public Builder ignoreWhiteSpaces(boolean val) {
            ignoreWhiteSpaces = val;
            return this;
        }

        /**
         * Set the tag used for displaying changes in the original text.
         * @param tag the tag to set. Without angle brackets. Default: {@value #DEFAULT_TAG_DELETE}.
         * @return builder with configured inlineOriginDeleteTag parameter
         * @deprecated Use {@link #inlineOriginDeleteTag(String)}
         */
        @Deprecated
        public Builder InlineOldTag(String tag) {
            inlineOriginDeleteTag = tag;
            return this;
        }

        /**
         * Set the tag used for displaying delete data in the original text.
         * @param tag the tag to set. Without angle brackets. Default: {@value #DEFAULT_TAG_DELETE}.
         * @return builder with configured inlineOriginDeleteTag parameter
         */
        public Builder inlineOriginDeleteTag(String tag) {
            inlineOriginDeleteTag = tag;
            return this;
        }

        /**
         * Set the tag used for displaying changes in the revised text.
         * @param tag the tag to set. Without angle brackets. Default: {@value #DEFAULT_TAG_INSERT}.
         * @return builder with configured inlineRevisedInsertTag parameter
         * @deprecated Use {@link #inlineRevisedInsertTag(String)}
         */
        public Builder InlineNewTag(String tag) {
            inlineRevisedInsertTag = tag;
            return this;
        }

        /**
         * Set the tag used for displaying changes in the revised text.
         * @param tag the tag to set. Without angle brackets. Default: {@value #DEFAULT_TAG_INSERT}.
         * @return builder with configured inlineRevisedInsertTag parameter
         */
        public Builder inlineRevisedInsertTag(String tag) {
            inlineRevisedInsertTag = tag;
            return this;
        }

        /**
         * Set the css class used for displaying changes in the original text.
         * @param cssClass the tag to set. Without any quotes, just word. Default: {@value #DEFAULT_CSSCLASS_DELETE}.
         * @return builder with configured inlineOriginDeleteCssClass parameter
         * @deprecated Use {@link #inlineOriginDeleteCssClass(String)}
         */
        public Builder InlineOldCssClass(String cssClass) {
            inlineOriginDeleteCssClass = cssClass;
            return this;
        }

        /**
         * Set the css class used for displaying delete data in the original text.
         * @param cssClass the tag to set. Without any quotes, just word. Default: {@value #DEFAULT_CSSCLASS_DELETE}.
         * @return builder with configured inlineOriginDeleteCssClass parameter
         */
        public Builder inlineOriginDeleteCssClass(String cssClass) {
            inlineOriginDeleteCssClass = cssClass;
            return this;
        }

        /**
         * Set the css class used for displaying changes in the revised text.
         * @param cssClass the tag to set. Without any quotes, just word. Default: {@value #DEFAULT_CSSCLASS_INSERT}.
         * @return builder with configured inlineRevisedInsertCssClass parameter
         * @deprecated Use {@link #inlineRevisedInsertCssClass(String)}
         */
        public Builder InlineNewCssClass(String cssClass) {
            inlineRevisedInsertCssClass = cssClass;
            return this;
        }

        /**
         * Set the css class used for displaying insert data in the revised text.
         * @param cssClass the tag to set. Without any quotes, just word. Default: {@value #DEFAULT_CSSCLASS_INSERT}.
         * @return builder with configured inlineRevisedInsertCssClass parameter
         */
        public Builder inlineRevisedInsertCssClass(String cssClass) {
            inlineRevisedInsertCssClass = cssClass;
            return this;
        }

        /**
         * Set the column with of generated lines of original and revised texts.
         * @param width the width to set. Making it < 0 disable line breaking.
         * @return builder with configured columnWidth parameter
         */
        public Builder columnWidth(int width) {
            columnWidth = width;
            return this;
        }

        @Nonnull
        public Builder defaultString(@Nullable String defaultString) {
            this.defaultString = defaultString;
            return this;
        }

        /**
         * Set the custom equalizer to use while comparing the lines of the revisions.
         * @param stringEqualizer to use (custom one)
         * @return builder with configured stringEqualizer
         */
        public Builder stringEqualizer(Equalizer<String> stringEqualizer) {
            this.diffAlgorithm = new MyersDiff<>(stringEqualizer);
            return this;
        }

        /**
         * Set the custom {@link DiffAlgorithm} to use while comparing the lines
         * of the revisions.
         * @param stringEqualizer to use (custom one)
         * @return builder with configured stringEqualizer
         */
        public Builder diffAlgorithm(DiffAlgorithm<String> diffAlgorithm) {
            this.diffAlgorithm = diffAlgorithm;
            return this;
        }

        /**
         * Build the DiffRowGenerator using the default Equalizer for rows.
         * If some parameters are not set, the default values are used.
         * @return the customized DiffRowGenerator
         */
        public DiffRowGenerator build() {
            return new DiffRowGenerator(this);
        }
    }

    private DiffRowGenerator(Builder builder) {
        showInlineDiffs = builder.showInlineDiffs;
        ignoreWhiteSpaces = builder.ignoreWhiteSpaces;

        inlineOriginDeleteTag = builder.inlineOriginDeleteTag;
        inlineOriginDeleteCssClass = builder.inlineOriginDeleteCssClass;

        inlineOriginChangeTag = builder.inlineOriginChangeTag;
        inlineOriginChangeCssClass = builder.inlineOriginChangeCssClass;

        inlineRevisedInsertTag = builder.inlineRevisedInsertTag;
        inlineRevisedInsertCssClass = builder.inlineRevisedInsertCssClass;

        inlineRevisedChangeTag = builder.inlineRevisedChangeTag;
        inlineRevisedChangeCssClass = builder.inlineRevisedChangeCssClass;

        columnWidth = builder.columnWidth;
        defaultString = builder.defaultString;
        diffAlgorithm = builder.diffAlgorithm;
    }

    /**
     * Get the DiffRows describing the difference between original and revised texts using the
     * given patch. Useful for displaying side-by-side diff.
     *
     * @param original the original text
     * @param revised the revised text
     * @return the DiffRows between original and revised texts
     */
    public List<DiffRow> generateDiffRows(List<String> original, List<String> revised) {
        if (ignoreWhiteSpaces) {
            Function<String, String> whiteSpaceReplacer = new Function<String, String>() {
                @Override
                public String apply(String string) {
                    if (string == null) {
                        return null;
                    } else {
                        return string.trim().replaceAll("\\s+", " ");
                    }
                }
            };
            original = Lists.transform(original, whiteSpaceReplacer);
            revised = Lists.transform(revised, whiteSpaceReplacer);
        }
        return generateDiffRows(original, revised, DiffUtils.diff(original, revised, diffAlgorithm));
    }

    /**
     * Generates the DiffRows describing the difference between original and revised texts using the
     * given patch. Useful for displaying side-by-side diff.
     *
     * @param original the original text
     * @param revised the revised text
     * @param patch the given patch
     * @return the DiffRows between original and revised texts
     */
    public List<DiffRow> generateDiffRows(List<String> original, List<String> revised, Patch<String> patch) {
        // normalize the lines (expand tabs, escape html entities)
        original = StringUtills.normalize(original);
        revised = StringUtills.normalize(revised);

        // wrap to the column width
        if (columnWidth > 0) {
            original = StringUtills.wrapText(original, this.columnWidth);
            revised = StringUtills.wrapText(revised, this.columnWidth);
        }
        List<DiffRow> diffRows = new ArrayList<DiffRow>();
        int orgEndPos = 0;
        int revEndPos = 0;
        final List<Delta<String>> deltaList = patch.getDeltas();
        for (int i = 0; i < deltaList.size(); i++) {
            Delta<String> delta = deltaList.get(i);
            Chunk<String> orig = delta.getOriginal();
            Chunk<String> rev = delta.getRevised();

            // We should normalize and wrap lines in deltas too.
            orig.setLines(StringUtills.normalize(orig.getLines()));
            rev.setLines(StringUtills.normalize(rev.getLines()));

            if (columnWidth > 0) {
                orig.setLines(StringUtills.wrapText(orig.getLines(), this.columnWidth));
                rev.setLines(StringUtills.wrapText(rev.getLines(), this.columnWidth));
            }
            // catch the equal prefix for each chunk
            copyEqualsLines(diffRows, original, orgEndPos, orig.getPosition(), revised, revEndPos, rev.getPosition());

            // Inserted DiffRow
            if (delta.getClass() == InsertDelta.class) {
                orgEndPos = orig.last() + 1;
                revEndPos = rev.last() + 1;
                for (String line : rev.getLines()) {
                    diffRows.add(new DiffRow(Tag.INSERT, defaultString, line));
                }
                continue;
            }

            // Deleted DiffRow
            if (delta.getClass() == DeleteDelta.class) {
                orgEndPos = orig.last() + 1;
                revEndPos = rev.last() + 1;
                for (String line : orig.getLines()) {
                    diffRows.add(new DiffRow(Tag.DELETE, line, defaultString));
                }
                continue;
            }

            if (showInlineDiffs) {
                addInlineDiffs(delta);
            }
            // the changed size is match
            if (orig.size() == rev.size()) {
                for (int j = 0; j < orig.size(); j++) {
                    diffRows.add(new DiffRow(Tag.CHANGE, orig.getLines().get(j), rev.getLines().get(j)));
                }
            } else if (orig.size() > rev.size()) {
                for (int j = 0; j < orig.size(); j++) {
                    diffRows.add(new DiffRow(Tag.CHANGE, orig.getLines().get(j),
                            rev.getLines().size() > j ? (String) rev.getLines().get(j) : defaultString));
                }
            } else {
                for (int j = 0; j < rev.size(); j++) {
                    diffRows.add(new DiffRow(Tag.CHANGE, orig.getLines().size() > j ? orig.getLines().get(j)
                            : defaultString, rev.getLines().get(j)));
                }
            }
            orgEndPos = orig.last() + 1;
            revEndPos = rev.last() + 1;
        }

        // Copy the final matching chunk if any.
        copyEqualsLines(diffRows, original, orgEndPos, original.size(), revised, revEndPos, revised.size());
        return diffRows;
    }

    protected void copyEqualsLines(List<DiffRow> diffRows, List<String> original, int originalStartPos,
            int originalEndPos, List<String> revised, int revisedStartPos, int revisedEndPos) {
        String[][] lines = new String[originalEndPos - originalStartPos][2];
        int idx = 0;
        for (String line : original.subList(originalStartPos, originalEndPos)) {
            lines[idx++][0] = line;
        }
        idx = 0;
        for (String line : revised.subList(revisedStartPos, revisedEndPos)) {
            lines[idx++][1] = line;
        }
        for (String[] line : lines) {
            diffRows.add(new DiffRow(Tag.EQUAL, line[0], line[1]));
        }
    }

    /**
     * Add the inline diffs for given delta
     * 
     * @param delta the given delta
     */
    private void addInlineDiffs(Delta<String> delta) {
        List<String> orig = delta.getOriginal().getLines();
        List<String> rev = delta.getRevised().getLines();
        LinkedList<String> origList = charArrayToStringList(LF_JOINER.join(orig).toCharArray());
        LinkedList<String> revList = charArrayToStringList(LF_JOINER.join(rev).toCharArray());

        List<Delta<String>> inlineDeltas = DiffUtils.diff(origList, revList).getDeltas();
        Collections.reverse(inlineDeltas);
        for (Delta<String> inlineDelta : inlineDeltas) {
            Chunk<String> inlineOrig = inlineDelta.getOriginal();
            Chunk<String> inlineRev = inlineDelta.getRevised();
            if (inlineDelta.getClass().equals(DeleteDelta.class)) {
                origList = wrapInTag(origList, inlineOrig.getPosition(), inlineOrig.getPosition() + inlineOrig.size()
                        + 1, this.inlineOriginDeleteTag, this.inlineOriginDeleteCssClass);
            } else if (inlineDelta.getClass().equals(InsertDelta.class)) {
                revList = wrapInTag(revList, inlineRev.getPosition(), inlineRev.getPosition() + inlineRev.size() + 1,
                        this.inlineRevisedInsertTag, this.inlineRevisedInsertCssClass);
            } else if (inlineDelta.getClass().equals(ChangeDelta.class)) {
                origList = wrapInTag(origList, inlineOrig.getPosition(), inlineOrig.getPosition() + inlineOrig.size()
                        + 1, this.inlineOriginChangeTag, this.inlineOriginChangeCssClass);
                revList = wrapInTag(revList, inlineRev.getPosition(), inlineRev.getPosition() + inlineRev.size() + 1,
                        this.inlineRevisedChangeTag, this.inlineRevisedChangeCssClass);
            }
        }

        delta.getOriginal().setLines(Arrays.asList(JOINER.join(origList).split(NEW_LINE)));
        delta.getRevised().setLines(Arrays.asList(JOINER.join(revList).split(NEW_LINE)));
    }

    private static final LinkedList<String> charArrayToStringList(char[] cs) {
        LinkedList<String> result = new LinkedList<String>();
        for (Character character : cs) {
            result.add(character.toString());
        }
        return result;
    }

    private static final Pattern PATTERN_CRLF = Pattern.compile("([\\n\\r]+)");

    /**
     * Wrap the elements in the sequence with the given tag
     * @param startPosition the position from which tag should start. The counting start from a zero.
     * @param endPosition the position before which tag should should be closed.
     * @param tag the tag name without angle brackets, just a word
     * @param cssClass the optional css class
     */
    public static LinkedList<String> wrapInTag(LinkedList<String> sequence, int startPosition, int endPosition,
            String tag, String cssClass) {
        LinkedList<String> result = (LinkedList<String>) sequence.clone();
        StringBuilder tagBuilder = new StringBuilder();
        tagBuilder.append("<");
        tagBuilder.append(tag);
        if (cssClass != null) {
            tagBuilder.append(" class=\"");
            tagBuilder.append(cssClass);
            tagBuilder.append("\"");
        }
        tagBuilder.append(">");
        final String startTag = tagBuilder.toString();

        tagBuilder.delete(0, tagBuilder.length());

        tagBuilder.append("</");
        tagBuilder.append(tag);
        tagBuilder.append(">");
        final String endTag = tagBuilder.toString();

        result.add(startPosition, startTag);
        result.add(endPosition, endTag);
        final String joinTag = new StringBuilder(Matcher.quoteReplacement(endTag)).append("$1")
                .append(Matcher.quoteReplacement(startTag)).toString();

        for (int i = startPosition + 1; i < endPosition; ++i) {
            final String val = result.get(i);
            if (val.contains("\n") || val.contains("\r")) {
                result.set(i, PATTERN_CRLF.matcher(val).replaceAll(joinTag));
            }
        }
        return result;
    }

    /**
     * Wrap the given line with the given tag
     * @param line the given line
     * @param tag the tag name without angle brackets, just a word
     * @param cssClass the optional css class
     * @return the wrapped string
     */
    public static String wrapInTag(String line, String tag, String cssClass) {
        final StringBuilder tagBuilder = new StringBuilder();
        tagBuilder.append("<");
        tagBuilder.append(tag);
        if (cssClass != null) {
            tagBuilder.append(" class=\"");
            tagBuilder.append(cssClass);
            tagBuilder.append("\"");
        }
        tagBuilder.append(">");
        final String startTag = tagBuilder.toString();

        tagBuilder.delete(0, tagBuilder.length());

        tagBuilder.append("</");
        tagBuilder.append(tag);
        tagBuilder.append(">");
        final String endTag = tagBuilder.toString();

        final String joinTag = new StringBuilder(Matcher.quoteReplacement(endTag)).append("$1")
                .append(Matcher.quoteReplacement(startTag)).toString();

        return startTag + PATTERN_CRLF.matcher(line).replaceAll(joinTag) + endTag;
    }
}