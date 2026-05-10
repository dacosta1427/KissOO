Generic question:
idx.indexDirect(doc) acts on its OWN thread and queue?
Is there no monitor in the MultiLanguageLuceneMgr that keeps track of the size of all the queues?
This way you would know when the things are empty. Or heve the queueReader update a flag or whatever/ send a signal to the MultiLanguageLuceneMgr that it (the particular reader) is empty. ??

Synchronous indexing is NOT an option becasue that will KILL performance!
The MultiLanguageLuceneMgr should ONLY be called from the main thread in defined situations. Default is the async queue!!
Maybe have 6 queues? Indert/update/delete (similar to the TC) and those 3 in a synch and asynch set? What do you think?


INSERT:
`indexDirect` writes __directly to the Lucene IndexWriter__ (bypassing the async queue) => **This is wrong, it should be queued in a default setting. It can only be an blocking action if this was explicitly specified in the TC!**

UPDATE:
Why not simply retrieve the doc (NOT rebuild it) via the old OID (get it via history if need be? Retrieve it in the lucene index via the OID field, which should be there ALWAYS!
There is a method called getPreviousVersion() so this should not be too difficult.
When this is pushed in the queue the new updated doc can also be created and pushed in its queue (the OID will be different so there should not be an issue).

DELETE:
Again WHY are you rebuilding a new LuceneDoc (LDoc) when you already have one in the Lin??






