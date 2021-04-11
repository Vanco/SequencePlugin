package org.intellij.sequencer.util;

import org.intellij.markdown.IElementType;
import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor;
import org.intellij.markdown.html.GeneratingProvider;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.parser.LinkMap;
import org.intellij.markdown.parser.MarkdownParser;

import java.util.Map;

public class MdUtil {
    public static String generateMarkdownHtml(String text) {

        final GFMFlavourDescriptor flavour = new GFMFlavourDescriptor();
        final ASTNode parsedTree = new MarkdownParser(flavour).buildMarkdownTreeFromString(text);
        final LinkMap linkMap = LinkMap.Builder.buildLinkMap(parsedTree, text);
        final Map<IElementType, GeneratingProvider> map = flavour.createHtmlGeneratingProviders(linkMap, null);

        return new HtmlGenerator(text, parsedTree, map, true).generateHtml();
    }
}