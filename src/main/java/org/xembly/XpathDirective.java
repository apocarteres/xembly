/**
 * Copyright (c) 2013, xembly.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the xembly.org nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.xembly;

import com.jcabi.aspects.Immutable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XPATH directive.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@Immutable
@EqualsAndHashCode(of = "expr")
final class XpathDirective implements Directive {

    /**
     * XPath to use.
     */
    private final transient Arg expr;

    /**
     * Public ctor.
     * @param path XPath
     * @throws XmlContentException If invalid input
     */
    XpathDirective(final String path) throws XmlContentException {
        this.expr = new Arg(path);
    }

    @Override
    public String toString() {
        return String.format("XPATH %s", this.expr);
    }

    @Override
    public Collection<Node> exec(final Document dom,
        final Collection<Node> current) throws ImpossibleModificationException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final Collection<Node> targets = new HashSet<Node>(0);
        for (final Node node : XpathDirective.roots(dom, current)) {
            final NodeList list;
            try {
                list = NodeList.class.cast(
                    xpath.evaluate(
                        this.expr.raw(),
                        node,
                        XPathConstants.NODESET
                    )
                );
            } catch (XPathExpressionException ex) {
                throw new ImpossibleModificationException(
                    String.format("invalid XPath expr '%s'", this.expr), ex
                );
            }
            final int len = list.getLength();
            for (int idx = 0; idx < len; ++idx) {
                targets.add(list.item(idx));
            }
        }
        return targets;
    }

    /**
     * Get roots to start searching from.
     * @param dom Document
     * @param nodes Current nodes
     * @return Root nodes to start searching from
     */
    private static Iterable<Node> roots(final Document dom,
        final Collection<Node> nodes) {
        final Collection<Node> roots;
        if (nodes.isEmpty()) {
            roots = Collections.<Node>singletonList(dom.getDocumentElement());
        } else {
            roots = nodes;
        }
        return roots;
    }

}