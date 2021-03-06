package org.jetbrains.spek.engine

import org.jetbrains.spek.api.lifecycle.ActionScope
import org.jetbrains.spek.api.lifecycle.GroupScope
import org.jetbrains.spek.api.lifecycle.LifecycleListener
import org.jetbrains.spek.api.lifecycle.TestScope
import java.util.LinkedHashMap
import java.util.LinkedList
import java.util.WeakHashMap

/**
 *
 * @author Ranie Jade Ramiso
 */
class FixturesAdapter: LifecycleListener {
    private val beforeEachTest: LinkedHashMap<GroupScope, LinkedList<() -> Unit>> = LinkedHashMap()
    private val afterEachTest: LinkedHashMap<GroupScope, LinkedList<() -> Unit>> = LinkedHashMap()

    private val beforeGroup: LinkedHashMap<GroupScope, LinkedList<() -> Unit>> = LinkedHashMap()
    private val afterGroup: LinkedHashMap<GroupScope, LinkedList<() -> Unit>> = LinkedHashMap()

    override fun beforeExecuteTest(test: TestScope) {
        if (test.parent !is ActionScope) {
            invokeAllBeforeEachTest(test.parent)
        }
    }

    override fun afterExecuteTest(test: TestScope) {
        if (test.parent !is ActionScope) {
            invokeAllAfterEachTest(test.parent)
        }
    }

    override fun beforeExecuteAction(action: ActionScope) {
        invokeAllBeforeEachTest(action)
    }

    override fun afterExecuteAction(action: ActionScope) {
        invokeAllAfterEachTest(action)
    }

    override fun beforeExecuteGroup(group: GroupScope) {
        beforeGroup[group]?.forEach { it() }
    }

    override fun afterExecuteGroup(group: GroupScope) {
        afterGroup[group]?.reversed()?.forEach { it() }
    }

    fun registerBeforeEachTest(group: GroupScope, callback: () -> Unit) {
        beforeEachTest.getOrPut(group, { LinkedList() }).add(callback)
    }

    fun registerAfterEachTest(group: GroupScope, callback: () -> Unit) {
        afterEachTest.getOrPut(group, { LinkedList() }).add(callback)
    }

    fun registerBeforeGroup(group: GroupScope, callback: () -> Unit) {
        beforeGroup.getOrPut(group, { LinkedList() }).add(callback)
    }

    fun registerAfterGroup(group: GroupScope, callback: () -> Unit) {
        afterGroup.getOrPut(group, { LinkedList() }).add(callback)
    }

    private fun invokeAllBeforeEachTest(group: GroupScope) {
        if (group.parent != null) {
            invokeAllBeforeEachTest(group.parent!!)
        }
        beforeEachTest[group]?.forEach { it.invoke() }
    }

    private fun invokeAllAfterEachTest(group: GroupScope) {
        afterEachTest[group]?.reversed()?.forEach { it.invoke() }
        if (group.parent != null) {
            invokeAllAfterEachTest(group.parent!!)
        }
    }
}
