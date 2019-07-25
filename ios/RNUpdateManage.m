//
//  RNUpdateManage.m
//  RNUpdate
//
//  Created by 放心家 on 2019/7/24.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTRootView.h>
#import <React/RCTBridge.h>
#import <React/RCTBundleURLProvider.h>
#import "RNUpdateManage.h"

#define FileManager [NSFileManager defaultManager]
#define APP_VERSION ([[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"])

@implementation RNUpdateManage

+ (BOOL)resetJSBundlePath {
    [FileManager removeItemAtPath: [self pathForBundle] error:nil];
    BOOL(^createBundle)(BOOL) = ^(BOOL retry) {
        NSError *error;
        if(![FileManager fileExistsAtPath: [self pathForBundle]]) {
            [FileManager createDirectoryAtPath:[self pathForBundle] withIntermediateDirectories:YES attributes:nil error:&error];
        }
        if (error) {
            if (retry) {
                createBundle(NO);
            } else {
                return NO;
            }
        }
        return YES;
    };
    return createBundle(YES);
}

+ (BOOL) copyFileToPath:(NSString *)originPath targetPath:(NSString *)targetPath {
    NSError *error = nil;
    [FileManager copyItemAtPath: originPath toPath:targetPath error: &error];
    if (error) {
        return NO;
    }
    return YES;
}

+ (BOOL)copyFileToURL:(NSURL *)originURL targetURL:(NSURL *)targetURL {
    NSError *error = nil;
    [FileManager copyItemAtURL: originURL toURL: targetURL error: &error];
    if (error) {
        return NO;
    }
    return YES;
}

+ (BOOL)copyJSBundleFileToURL:(NSURL *)url {
    NSURL *bundleFileURL = [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];
    return [self copyFileToPath: bundleFileURL targetPath: url];
}

+ (BOOL)copyAssetsFileToURL:(NSURL *)url {
    NSURL *assetsFileURL = [[NSBundle mainBundle] URLForResource:@"assets" withExtension:nil];
    return [self copyFileToURL:assetsFileURL targetURL:url];
}

+ (RCTRootView *)getRootViewModuleName:(NSString *)moduleName
                         launchOptions:(NSDictionary *)launchOptions {
    RCTRootView *rootView;
    NSURL *jsCodeLocation;
#if DEBUG
    jsCodeLocation = [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index" fallbackResource:nil];
    rootView = [self createRootViewWithURL:jsCodeLocation moduleName:moduleName launchOptions:launchOptions];
#else
    jsCodeLocation = [self URLForJSBundleInDocumentsDirectory];
    BOOL isBundleExists = [FileManager fileExistsAtPath: [self pathForJSBundleInDocumentsDirectory]];
    BOOL isAsssetsExists = [FileManager fileExistsAtPath: [self pathForAssetsInDocumentsDirectory]];
    if (!isBundleExists) {
        [self resetJSBundlePath];
        BOOL copyJSBundleResult = [self copyJSBundleFileToURL:[self URLForJSBundleInDocumentsDirectory]];
        if (!copyJSBundleResult) {
            [self resetJSBundlePath];
            jsCodeLocation = [self URLForJSBundleInProject];
        }
    }
    if (!isAsssetsExists) {
        BOOL copyAssetsResult = [self copyAssetsFileToURL:[self URLForAssetsInDocumentsDirectory]];
        if(!copyAssetsResult) {
            [self resetJSBundlePath];
            jsCodeLocation = [self URLForJSBundleInProject];
        }
    }
    RCTBridge *bridge = [self createBridgeWithBundleURL:jsCodeLocation];
    rootView = [self createRootViewWithModuleName:moduleName bridge:bridge];
#endif
    return rootView;
}

+ (RCTBridge *)createBridgeWithBundleURL:(NSURL *)bundleURL {
    return [[RCTBridge alloc] initWithBundleURL:bundleURL moduleProvider:nil launchOptions:nil];
}

+ (RCTRootView *)createRootViewWithURL:(NSURL *)url
                            moduleName:(NSString *)moduleName
                         launchOptions:(NSDictionary *)launchOptions {
    return [[RCTRootView alloc] initWithBundleURL:url
                                       moduleName:moduleName
                                initialProperties:nil
                                    launchOptions:launchOptions];
}

+ (RCTRootView *)createRootViewWithModuleName:(NSString *)moduleName
                                       bridge:(RCTBridge *)bridge {
    return [[RCTRootView alloc] initWithBridge:bridge moduleName:moduleName initialProperties:nil];
}

+ (NSURL *)URLForJSBundleInProject {
    return [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];
}

+ (NSURL *)URLForAssetsInProject {
    NSURL *assetsFileURL = [[NSBundle mainBundle] URLForResource:@"assets" withExtension:nil];
    return assetsFileURL;
}

+ (NSURL *)URLForJSBundleInDocumentsDirectory {
    return [NSURL fileURLWithPath:[self pathForJSBundleInDocumentsDirectory]];
}

+ (NSURL *)URLForAssetsInDocumentsDirectory {
    return [NSURL fileURLWithPath:[self pathForAssetsInDocumentsDirectory]];
}

+ (NSString *)pathForJSBundleInDocumentsDirectory {
    NSString *fileName = [@"main" stringByAppendingPathExtension:@"jsbundle"];
    NSString *filePath = [[self pathForBundle] stringByAppendingPathComponent: fileName];
    return filePath;
}

+ (NSString *)pathForAssetsInDocumentsDirectory {
    NSString *folderName = @"assets";
    NSString *folderPath = [[self pathForBundle] stringByAppendingPathComponent:folderName];
    return folderPath;
}

+ (NSString *)pathForBundle {
    NSString *documentsPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject];
    NSString *bundlePath = [[documentsPath stringByAppendingPathComponent:@"JSBundle"] stringByAppendingPathComponent:[@"ios_" stringByAppendingString:APP_VERSION]];
    return bundlePath;
}

@end
