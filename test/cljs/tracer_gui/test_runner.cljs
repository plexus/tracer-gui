(ns tracer-gui.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [tracer-gui.core-test]))

(enable-console-print!)

(doo-tests 'tracer-gui.core-test)
