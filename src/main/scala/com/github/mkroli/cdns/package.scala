/*
 * Copyright 2014 Michael Krolikowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mkroli

import scala.Vector
import scala.annotation.tailrec
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Promise
import scala.language.implicitConversions
import scala.language.postfixOps
import scala.math.random

import com.google.common.util.concurrent.ListenableFuture

package object cdns {
  implicit def functionToRunnable[T](f: () => T) = new Runnable {
    override def run = f()
  }

  implicit def listenableFutureToFuture[T](lf: ListenableFuture[T])(implicit c: ExecutionContextExecutor) = {
    val promise = Promise[T]
    lf.addListener(() => promise.success(lf.get), c)
    promise.future
  }

  implicit class RichIndexedSeq[T](s: IndexedSeq[T]) {
    def shuffle = {
      @tailrec
      def freeIndexFrom(taken: Set[Int], from: Int): Int = {
        if (taken contains from)
          freeIndexFrom(taken, (from + 1) % s.size)
        else
          from
      }

      @tailrec
      def shuffle(taken: Set[Int], shuffled: IndexedSeq[T]): IndexedSeq[T] = {
        if (taken.size == s.size)
          shuffled
        else {
          val i = freeIndexFrom(taken, random * s.size toInt)
          shuffle(taken + i, s(i) +: shuffled)
        }
      }

      shuffle(Set(), Vector())
    }
  }
}
