using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Update.RNUpdate
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNUpdateModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNUpdateModule"/>.
        /// </summary>
        internal RNUpdateModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNUpdate";
            }
        }
    }
}
