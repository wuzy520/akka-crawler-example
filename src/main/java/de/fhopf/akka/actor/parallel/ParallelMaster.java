package de.fhopf.akka.actor.parallel;

import akka.actor.*;
import akka.routing.RoundRobinRouter;
import de.fhopf.akka.IndexerImpl;
import de.fhopf.akka.PageContent;
import de.fhopf.akka.PageRetriever;
import de.fhopf.akka.VisitedPageStore;
import de.fhopf.akka.actor.IndexedMessage;
import de.fhopf.akka.actor.IndexingActor;
import de.fhopf.akka.actor.Master;
import de.fhopf.akka.actor.PageParsingActor;
import java.util.concurrent.CountDownLatch;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetches and parses pages in parallel.
 * @author flo
 */
class ParallelMaster extends Master {

    private final Logger logger = LoggerFactory.getLogger(ParallelMaster.class);
    
    private final ActorRef parser;
    private final ActorRef indexer;
    
    public ParallelMaster(final IndexWriter indexWriter, final PageRetriever pageRetriever, final CountDownLatch latch) {
        super(latch);
        parser = getContext().actorOf(new Props(new UntypedActorFactory() {

            @Override
            public Actor create() {
                return new PageParsingActor(pageRetriever);
            }
        }).withRouter(new RoundRobinRouter(10)));
        indexer = getContext().actorOf(new Props(new UntypedActorFactory() {

            @Override
            public Actor create() {
                return new IndexingActor(new IndexerImpl(indexWriter));
            }
        }));
    }

    @Override
    protected ActorRef getIndexer() {
        return indexer;
    }

    @Override
    protected ActorRef getParser() {
        return parser;
    }
    
    
}