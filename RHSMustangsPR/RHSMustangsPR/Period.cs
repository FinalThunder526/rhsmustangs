﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace RHSMustangsPR
{
    class Period
    {
        public string periodShort;
        public string overrideName;
        public int startH, startM, endH, endM;
        public int groupN;

        /// <summary>
        /// Returns the parsed String to then save to the updates file.
        /// </summary>
        public override String ToString()
        {
            return periodShort + " " + ((overrideName == null) ? "-" : overrideName) + " " + startH + " " + startM + " " + endH + " " + endM + " " + groupN;
        }
    }
}