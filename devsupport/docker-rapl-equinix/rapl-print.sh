#!/usr/bin/env bash

psys_file="/sys/class/powercap/intel-rapl:1/energy_uj"
if [ -e "${psys_file}" ]; then
  psys="$(sudo cat "${psys_file}")"
  printf "Psys:           %12s\n" ${psys}
fi
package_file="/sys/class/powercap/intel-rapl:0/energy_uj"
if [ -e "${package_file}" ]; then
  package="$(sudo cat "${package_file}")"
  printf "Package:        %12s\n" ${package}
fi
core_file="/sys/class/powercap/intel-rapl:0:0/energy_uj"
if [ -e "${core_file}" ]; then
  core="$(sudo cat "${core_file}")"
  printf "  PP0 (Core):   %12s\n" ${core}
fi
uncore_file="/sys/class/powercap/intel-rapl:0:1/energy_uj"
if [ -e "${uncore_file}" ]; then
  uncore="$(sudo cat "${uncore_file}")"
  printf "  PP1 (Uncore): %12s\n" ${uncore}
fi
dram_file="/sys/class/powercap/intel-rapl:0:2/energy_uj"
if [ -e "${dram_file}" ]; then
  dram="$(sudo cat "${dram_file}")"
  printf "  DRAM:         %12s\n" ${dram}
fi
