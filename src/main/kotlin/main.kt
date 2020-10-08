import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

fun main() {
    val processor = Schedulers.newSingle("example-publish")
    Flux.fromIterable(listOf(Flux.fromIterable(List(100000) { it })))
        .flatMap {
            val processed = it.publishOn(processor)
                .flatMap { if (it % 4 == 0) Mono.just(it) else Mono.empty()  }
                .doOnComplete { println("completed") }
                .publish()
            val sub = MockSub()
            processed.doOnSubscribe(sub::onSubscribe)
                .doOnError(sub::onError)
                .doOnComplete(sub::onComplete)
                .subscribe() // because subscribe(subscriber) does not work
//            if uncommented, it still works
//            Thread.sleep(1000)
            processed.connect()
//            if uncommented, it does not work
//            Thread.sleep(1000)
            processed
        }.doFinally {
            println("FINALLY")
            processor.dispose()
        }.blockLast()
}

class MockSub : Subscriber<Int> {
    override fun onSubscribe(s: Subscription?) {
        println("ON SUB")
    }

    override fun onNext(t: Int?) {
    }

    override fun onError(t: Throwable?) {
        println("ERROR $t")
    }

    override fun onComplete() {
        println("COMPLETE")
    }

}
