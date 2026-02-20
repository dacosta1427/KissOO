package org.garret.perst.continuous;

/**
 * Exception thown by full text search query parser
 */
public class FullTextSearchQuerySyntaxError extends ContinuousException 
{
    private static final long serialVersionUID = 1L;

    public FullTextSearchQuerySyntaxError(Exception x) {
        super(x);
    }
}