package swagger.grails4.compiler


import groovy.transform.CompileStatic
import org.codehaus.groovy.antlr.LineColumn
import org.codehaus.groovy.antlr.SourceBuffer
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import swagger.grails4.openapi.ApiDocComment

/**
 * Extract fields comments to @ApiDocComment annotations.
 * If the class has @ApiDoc annotation then the class is subject to this transformer.
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class ApiDocTransformer extends AbstractASTTransformation {

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        // only process class node
        if (!(nodes[1] instanceof ClassNode)) {
            return
        }
        ClassNode classNode = nodes[1] as ClassNode
        // add @ApiDocComment to the class
        // add @ApiDocComment to the fields
        classNode.fields.eachWithIndex { field, index ->
            LineColumn endOfPrevNode = findPrevEndOfNode(field)
            LineColumn startOfCurrentNode = new LineColumn(field.lineNumber, field.columnNumber)
            String extractedComment
            if (endOfPrevNode.line == 0) {
                extractedComment = extractCommentLiterally(source, startOfCurrentNode)
            } else {
                extractedComment = extractComment(source, endOfPrevNode, startOfCurrentNode)
            }
            addApiDocCommentAnnotation(field, extractedComment)
        }
    }

    static LineColumn findPrevEndOfNode(FieldNode fieldNode) {
        int prevLine = 0
        int prevColumn = 0
        // try to find previous field if any
        def classNode = fieldNode.owner
        List<AnnotatedNode> nodes = []
        nodes.addAll(classNode.methods)
        nodes.addAll(classNode.fields)
        nodes.each { AnnotatedNode it ->
            // up from current node
            if (it.lastLineNumber < fieldNode.lineNumber ||
                    it.lastLineNumber == fieldNode.lineNumber && it.lastColumnNumber < fieldNode.columnNumber) {
                // try to find nearest node
                if (it.lastLineNumber > prevLine || it.lastLineNumber == prevLine && it.lastColumnNumber > prevColumn){
                    prevLine = it.lastLineNumber
                    prevColumn = it.lastColumnNumber
                }
            }
        }
        new LineColumn(prevLine, prevColumn)
    }

    static String extractComment(SourceUnit sourceUnit, LineColumn start, LineColumn end) {
        String snippet = populateSourceBuffer(sourceUnit).getSnippet(start, end)
        cleanComment(snippet)
    }

    static SourceBuffer populateSourceBuffer(SourceUnit sourceUnit) {
        def srcBuffer = new SourceBuffer()
        def reader = sourceUnit.source.reader
        // copy source code to SourceBuffer
        int ch
        while ((ch = reader.read()) != -1) {
            srcBuffer.write(ch)
        }
        return srcBuffer
    }
    /**
     * remove /&#42;*, *, &#42;/, // , comment marks and leading blanks
     */
    static String cleanComment(String snippet) {
        if (!snippet) {
            return ""
        }
        StringBuilder builder = new StringBuilder()
        snippet.eachLine { line ->
            String stripLine = line.trim()
            stripLine = stripLine.replaceFirst(~/^(\/\*\*|\/\*|\/\/|\*\/|\*)/, "")
            stripLine = stripLine.trim()
            if (stripLine) {
                builder << stripLine << "\n"
            }
        }
        builder.toString().trim()
    }

    static void addApiDocCommentAnnotation(FieldNode fieldNode, String comment) {
        def annotationNode = new AnnotationNode(ClassHelper.make(ApiDocComment))
        annotationNode.runtimeRetention = true
        annotationNode.addMember("value", new ConstantExpression(comment))
        fieldNode.addAnnotation(annotationNode)
    }

    /**
     * Extract comments from endLineColumn position upwards to the begins, stop when hit "{" or "}" not in
     * comment blocks.
     * Extract only one comments block nearest to field.
     *
     * @param sourceUnit
     * @param endLineColumn
     * @return
     */
    static String extractCommentLiterally(SourceUnit sourceUnit, LineColumn endLineColumn) {
        def srcBuffer = populateSourceBuffer(sourceUnit)
        def commentBuffer = new StringBuilder()
        def start = new LineColumn(endLineColumn.line - 1, 1)
        def end = endLineColumn
        def starStack = []
        boolean finish = false
        boolean hitOneStarCommentBlock = false
        while (start.line >= 1) {
            if (finish || hitOneStarCommentBlock) {
                return commentBuffer.toString()
            }
            String snippet = srcBuffer.getSnippet(start, end)
            // end early if hit '{' or '}' when not in comment block
            // find // start
            snippet.eachLine { line ->
                if (finish || hitOneStarCommentBlock) {
                    return
                }
                // match "//" comment line
                def slashPattern = ~$/^\s*///$
                def startStarPattern = ~$/^\s*/\*+/$
                def endStarPattern = ~$/\*/\s*/$
                def singleStarPattern = ~$/\s*\*+/$
                def blockDelimiterPattern = ~$/[{}]/$

                def slashMatcher = slashPattern.matcher(line)
                def endStarMatcher = endStarPattern.matcher(line)
                def startStarMatcher = startStarPattern.matcher(line)
                def singleStarMatcher = singleStarPattern.matcher(line)
                def blockDelimiterMatcher = blockDelimiterPattern.matcher(line)
                // slash comments do not affect star comment block stack status
                if (slashMatcher) {
                    commentBuffer.insert(0, slashMatcher.replaceFirst("").trim())
                    return
                }
                if (blockDelimiterMatcher) {
                    finish = true
                    return
                }
                if (starStack.empty) {
                    // not in comment block
                    if (endStarMatcher) {
                        starStack.push("*/")
                        commentBuffer.insert(0, endStarMatcher.replaceFirst("").trim())
                    } else if (startStarMatcher) {
                        println "Invalid comment block! start '/*' without matching end '*/' in line ${endLineColumn.line}"
                    }
                } else {
                    // in comment block
                    if (startStarMatcher) {
                        starStack.pop()
                        commentBuffer.insert(0, startStarMatcher.replaceFirst("").trim())
                        hitOneStarCommentBlock = true
                    } else if (singleStarMatcher) {
                        // remove leading star
                        commentBuffer.insert(0, singleStarMatcher.replaceFirst("").trim())
                    } else {
                        commentBuffer.insert(0, line.trim())
                    }
                }
            }
            end = start
            start = new LineColumn(start.line - 1, 1)
        }
        commentBuffer.toString()
    }
}
